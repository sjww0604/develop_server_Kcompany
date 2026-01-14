package org.example.develop_server_kcompany.payment.event;

import java.util.List;

import org.example.develop_server_kcompany.payment.domain.Order;
import org.example.develop_server_kcompany.payment.domain.OrderItem;
import org.example.develop_server_kcompany.payment.messaging.OrderEventProducer;
import org.example.develop_server_kcompany.payment.messaging.dto.OrderPaidEventItemPayload;
import org.example.develop_server_kcompany.payment.messaging.dto.OrderPaidEventPayload;
import org.example.develop_server_kcompany.payment.repository.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OrderCompletedEventListener 클래스입니다.
 * <p>
 * 주문이 결제 완료로 확정된 이후(트랜잭션 커밋 이후),
 * 주문 내역을 데이터 수집 플랫폼(Kafka)으로 실시간 전송하는 이벤트 리스너입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCompletedEventListener {

	private final OrderRepository orderRepository;
	private final OrderEventProducer orderEventProducer;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(OrderCompletedDomainEvent event) {
		Long orderId = event.getOrderId();

		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다. orderId=" + orderId));

		OrderPaidEventPayload payload = toPayload(order);
		orderEventProducer.sendOrderCompletedEvent(payload);

		log.info("[KAFKA] 주문 내역 전송 완료. orderId={}, userId={}, totalAmount={}",
			payload.getOrderId(), payload.getUserId(), payload.getTotalAmount());
	}

	private OrderPaidEventPayload toPayload(Order order) {
		List<OrderPaidEventItemPayload> items = order.getItems().stream()
			.map(this::toItem)
			.toList();

		return OrderPaidEventPayload.builder()
			.orderId(order.getId())
			.userId(order.getUserId())
			.totalAmount(order.getTotalAmount())
			.paidAt(order.getLastModifiedAt() != null ? order.getLastModifiedAt().toString() : null)
			.items(items)
			.build();
	}

	private OrderPaidEventItemPayload toItem(OrderItem item) {
		return OrderPaidEventItemPayload.builder()
			.menuId(item.getMenuId())
			.quantity(item.getQuantity())
			.unitPriceSnapshot(item.getUnitPriceSnapshot())
			.lineAmount(item.getLineAmount())
			.build();
	}
}
