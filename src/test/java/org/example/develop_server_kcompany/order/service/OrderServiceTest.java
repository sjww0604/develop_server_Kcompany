package org.example.develop_server_kcompany.order.service;

import static org.assertj.core.api.Assertions.*;
import static org.example.develop_server_kcompany.payment.service.OrderService.*;
import static org.example.develop_server_kcompany.point.service.PointService.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.example.develop_server_kcompany.common.exception.CustomException;
import org.example.develop_server_kcompany.common.exception.ErrorCode;
import org.example.develop_server_kcompany.menu.domain.Menu;
import org.example.develop_server_kcompany.menu.repository.MenuRepository;
import org.example.develop_server_kcompany.payment.domain.Order;
import org.example.develop_server_kcompany.payment.enums.OrderStatus;
import org.example.develop_server_kcompany.payment.repository.OrderRepository;
import org.example.develop_server_kcompany.payment.service.OrderService;
import org.example.develop_server_kcompany.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/**
 * OrderServiceTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스: {@link OrderService}
 * 대상 메서드: 주문 생성/결제 유스케이스(createOrder)
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 14.
 */

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrderServiceTest {

	@Mock
	OrderRepository orderRepository;
	@Mock
	PointService pointService;
	@Mock
	MenuRepository menuRepository;
	@Mock
	ApplicationEventPublisher eventPublisher;

	@InjectMocks
	OrderService orderService;

	private Menu activeMenu(Long id, String name, Integer price) {
		try {
			Constructor<Menu> ctor = Menu.class.getDeclaredConstructor();
			ctor.setAccessible(true);
			Menu menu = ctor.newInstance();
			setField(menu, "id", id);
			setField(menu, "name", name);
			setField(menu, "price", price);
			setFieldIfExists(menu, "isActive", true);
			return menu;
		} catch (Exception e) {
			throw new IllegalStateException("테스트용 Menu 생성 실패", e);
		}
	}

	private void setField(Object target, String fieldName, Object value) {
		try {
			Field f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (Exception e) {
			throw new IllegalStateException("필드 주입 실패: " + fieldName, e);
		}
	}

	private void setFieldIfExists(Object target, String fieldName, Object value) {
		try {
			Field f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (NoSuchFieldException ignore) {
			// ignore
		} catch (Exception e) {
			throw new IllegalStateException("필드 주입 실패: " + fieldName, e);
		}
	}

	@Test
	@DisplayName("주문 생성 성공: 포인트 차감(orderId 포함) 호출 및 응답 반환")
	void createOrder_success_marksPaid_and_spendWithOrderId() {
		Long userId = 1L;
		String key = "k1";
		List<CreateOrderItemCommand> items = List.of(new CreateOrderItemCommand(10L, 2));

		when(orderRepository.findByUserIdAndIdempotencyKey(eq(userId), anyString()))
			.thenReturn(Optional.empty());

		when(menuRepository.findById(10L))
			.thenReturn(Optional.of(activeMenu(10L, "아메리카노", 3000)));

		// save() 이후 주문 ID가 생성된 상황을 가정하기 위해 저장 대상 Order에 id를 주입해 반환
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
			Order o = inv.getArgument(0);
			setField(o, "id", 1L);
			return o;
		});

		when(pointService.spend(eq(userId), anyLong(), anyString(), anyLong()))
			.thenReturn(SpendResult.of(false, 10000L, key));

		CreateOrderResult result = orderService.createOrder(userId, key, items);

		// spend 호출 검증 (orderId 포함)
		verify(pointService).spend(eq(userId), anyLong(), anyString(), anyLong());

		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.items()).hasSize(1);
	}

	@Test
	@DisplayName("멱등성 키 중복 케이스 검증")
	void createOrder_whenExistingOrder_thenResumeAndReturnDuplicateTrue() {
		Long userId = 1L;
		String key = "k1";
		List<CreateOrderItemCommand> items = List.of(new CreateOrderItemCommand(10L, 1));

		Order existing = mock(Order.class);
		when(existing.getId()).thenReturn(99L);
		when(existing.getUserId()).thenReturn(userId);
		when(existing.getTotalAmount()).thenReturn(3000L);
		when(existing.getStatus()).thenReturn(OrderStatus.CREATED);

		when(orderRepository.findByUserIdAndIdempotencyKey(eq(userId), anyString()))
			.thenReturn(Optional.of(existing));

		when(pointService.spend(eq(userId), eq(3000L), anyString(), eq(99L)))
			.thenReturn(SpendResult.of(false, 7000L, key));

		CreateOrderResult result = orderService.createOrder(userId, key, items);

		verify(pointService).spend(eq(userId), eq(3000L), anyString(), eq(99L));
		verify(existing).markPaid();
		assertThat(result.duplicate()).isTrue();
	}

	@Test
	@DisplayName("주문 목록이 비어있는 경우")
	void createOrder_whenItemsEmpty_throwIllegalArgument() {
		assertThatThrownBy(() -> orderService.createOrder(1L, "k", List.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("장바구니는 비어있을 수 없습니다.");
	}

	@Test
	@DisplayName("등록되지 않은 메뉴를 담았을 경우 검증")
	void createOrder_whenMenuNotFound_throwCustomException() {
		when(orderRepository.findByUserIdAndIdempotencyKey(anyLong(), anyString()))
			.thenReturn(Optional.empty());
		when(menuRepository.findById(10L)).thenReturn(Optional.empty());

		List<CreateOrderItemCommand> items = List.of(new CreateOrderItemCommand(10L, 1));

		assertThatThrownBy(() -> orderService.createOrder(1L, "k", items))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MENU_NOT_FOUND);
	}

	@Test
	@DisplayName("동시성 문제 발생 시 검증")
	void createOrder_whenSaveConflict_thenFindAndResume() {
		Long userId = 1L;
		String key = "k1";
		List<CreateOrderItemCommand> items = List.of(new CreateOrderItemCommand(10L, 1));

		when(menuRepository.findById(10L))
			.thenReturn(Optional.of(activeMenu(10L, "라떼", 3000)));

		when(orderRepository.save(any(Order.class)))
			.thenThrow(new DataIntegrityViolationException("dup"));

		Order conflict = mock(Order.class);
		when(conflict.getId()).thenReturn(77L);
		when(conflict.getUserId()).thenReturn(userId);
		when(conflict.getTotalAmount()).thenReturn(3000L);
		when(conflict.getStatus()).thenReturn(OrderStatus.CREATED);
		when(conflict.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
		when(conflict.getItems()).thenReturn(List.of());

		when(orderRepository.findByUserIdAndIdempotencyKey(eq(userId), anyString()))
			.thenReturn(Optional.empty(), Optional.of(conflict));

		when(pointService.spend(eq(userId), eq(3000L), anyString(), eq(77L)))
			.thenReturn(SpendResult.of(true, 5000L, key));

		CreateOrderResult result = orderService.createOrder(userId, key, items);

		verify(pointService).spend(eq(userId), eq(3000L), anyString(), eq(77L));
		assertThat(result.duplicate()).isTrue();
	}
}
