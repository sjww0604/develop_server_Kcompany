package org.example.develop_server_kcompany.payment.dto;

import org.example.develop_server_kcompany.payment.service.OrderService.CreateOrderResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성 응답 DTO입니다.
 * <p>
 * 주문 생성 유스케이스 처리 결과를 클라이언트에 반환하기 위한 응답 객체입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@NoArgsConstructor
public class OrderResponse {

	private Long orderId;
	private Long userId;
	private long totalAmount;
	private long balanceAfter;
	private List<OrderItemResponse> items;
	private String idempotencyKey;
	private boolean duplicate;
	private LocalDateTime createdAt;

	public static OrderResponse from(CreateOrderResult result) {
		OrderResponse response = new OrderResponse();
		response.orderId = result.orderId();
		response.userId = result.userId();
		response.totalAmount = result.totalAmount();
		response.balanceAfter = result.balanceAfter();
		response.items = result.items().stream()
			.map(OrderItemResponse::from)
			.collect(Collectors.toList());
		response.idempotencyKey = result.idempotencyKey();
		response.duplicate = result.duplicate();
		response.createdAt = result.createdAt();
		return response;
	}
}
