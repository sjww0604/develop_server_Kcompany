package org.example.develop_server_kcompany.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.develop_server_kcompany.payment.domain.Order;
import org.example.develop_server_kcompany.payment.domain.OrderItem;
import org.example.develop_server_kcompany.payment.enums.OrderStatus;
import org.example.develop_server_kcompany.payment.repository.OrderRepository;
import org.example.develop_server_kcompany.point.service.PointService;
import org.example.develop_server_kcompany.point.service.PointService.SpendResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * <p>
 * 이 서비스는 단순히 주문을 저장하는 것에 그치지 않고,
 * 주문 생성 과정에서 반드시 지켜야 할 비즈니스 규칙들을 함께 처리합니다.
 * </p>
 *
 * <h3>주요 책임</h3>
 * <ul>
 * 	<li>멱등성 키를 이용한 중복 주문 방지</li>
 *  <li>주문 금액 계산 및 주문 데이터 저장</li>
 *  <li>사용자 포인트 차감 처리</li>
 *  <li>주문 생성 완료 후, 주문 내역을 외부 데이터 수집 플랫폼으로 전송</li>
 * </ul>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderRepository orderRepository;
	private final PointService pointService;

	@Transactional
	public CreateOrderResult createOrder(Long userId, String idempotencyKey, List<CreateOrderItemCommand> items) {

		String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
		validateCreateOrderRequest(items);

		Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKey(userId, normalizedKey);
		if (existing.isPresent()) {
			Order existingOrder = existing.get();
			return resumeIfNeeded(existingOrder, userId, normalizedKey, true);
		}

		Order order = Order.create(userId, normalizedKey);
		for (CreateOrderItemCommand item : items) {
			validateCreateOrderItem(item);
			order.addItem(
				OrderItem.create(item.menuId(), item.menuNameSnapshot(), item.unitPriceSnapshot(), item.quantity())
			);
		}

		long totalAmount = order.getTotalAmount();
		if (totalAmount <= 0) {
			throw new IllegalArgumentException("상품 총 가격은 양수여야 합니다.");
		}

		Order saved;
		try {
			saved = orderRepository.save(order);
		} catch (DataIntegrityViolationException e) {
			log.info("동시 요청 감지, 기존 주문 조회: userId={}, key={}", userId, normalizedKey);
			Order conflictOrder = orderRepository.findByUserIdAndIdempotencyKey(userId, normalizedKey)
				.orElseThrow(() -> e);

			return resumeIfNeeded(conflictOrder, userId, normalizedKey, true);
		}

		SpendResult spendResult = pointService.spend(userId, totalAmount, normalizedKey);
		saved.markPaid();

		return CreateOrderResult.from(saved, spendResult, false);
	}

	private CreateOrderResult resumeIfNeeded(
		Order order, Long userId, String normalizedKey, boolean requestDuplicate) {

		long totalAmount = order.getTotalAmount();
		if (totalAmount <= 0) {
			throw new IllegalStateException("주문 totalAmount가 유효하지 않습니다. orderId: " + order.getId());
		}

		SpendResult spendResult = pointService.spend(userId, totalAmount, normalizedKey);

		if (order.getStatus() == OrderStatus.CREATED) {
			order.markPaid();
		}

		return CreateOrderResult.from(order, spendResult, requestDuplicate || spendResult.duplicate());
	}

	private void validateCreateOrderRequest(List<CreateOrderItemCommand> items) {
		if (items == null || items.isEmpty()) {
			throw new IllegalArgumentException("장바구니는 비어있을 수 없습니다.");
		}
	}

	private String normalizeIdempotencyKey(String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return java.util.UUID.randomUUID().toString();
		}
		return idempotencyKey.trim();
	}

	private void validateCreateOrderItem(CreateOrderItemCommand item) {
		if (item == null) {
			throw new IllegalArgumentException("최소 1개 이상의 상품이 담겨야 합니다.");
		}

		// 아래 값들은 "주문 금액 계산"과 "스냅샷 저장"의 정합성을 위한 비즈니스 검증입니다.
		if (item.menuId() == null || item.menuId() <= 0) {
			throw new IllegalArgumentException("등록된 상품 식별자만 주문 생성이 가능합니다.");
		}
		if (item.menuNameSnapshot() == null || item.menuNameSnapshot().isBlank()) {
			throw new IllegalArgumentException("등록된 상품명만 주문 생성이 가능합니다.");
		}
		if (item.unitPriceSnapshot() == null || item.unitPriceSnapshot() <= 0) {
			throw new IllegalArgumentException("상품 가격은 양수여야 합니다.");
		}
		if (item.quantity() <= 0) {
			throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
		}
	}

	/**
	 * 컨트롤러(Request DTO)와 분리된, 서비스 내부 입력 커맨드입니다.
	 */
	public record CreateOrderItemCommand(Long menuId, String menuNameSnapshot, Long unitPriceSnapshot, int quantity) {
	}

	/**
	 * 주문 생성 결과(서비스 반환 값)입니다.
	 */
	public record CreateOrderResult(
		Long orderId,
		Long userId,
		long totalAmount,
		long balanceAfter,
		List<CreateOrderItemResult> items,
		String idempotencyKey,
		boolean duplicate,
		LocalDateTime createdAt
	) {
		public static CreateOrderResult from(Order order, SpendResult spendResult, boolean duplicate) {
			List<CreateOrderItemResult> itemResults = order.getItems().stream()
				.map(CreateOrderItemResult::from)
				.collect(Collectors.toList());

			return new CreateOrderResult(
				order.getId(),
				order.getUserId(),
				order.getTotalAmount(),
				spendResult.balanceAfter(),
				itemResults,
				spendResult.idempotencyKey(),
				duplicate,
				order.getCreatedTime()
			);
		}
	}

	public record CreateOrderItemResult(
		Long menuId,
		String menuName,
		Long unitPrice,
		int quantity,
		long lineAmount
	) {
		public static CreateOrderItemResult from(OrderItem item) {
			return new CreateOrderItemResult(
				item.getMenuId(),
				item.getMenuNameSnapshot(),
				item.getUnitPriceSnapshot(),
				item.getQuantity(),
				item.getLineAmount()
			);
		}
	}
}
