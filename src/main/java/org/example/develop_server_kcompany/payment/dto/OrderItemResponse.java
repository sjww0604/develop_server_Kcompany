package org.example.develop_server_kcompany.payment.dto;

import org.example.develop_server_kcompany.payment.service.OrderService.CreateOrderItemResult;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OrderItemResponse 클래스입니다.
 * <p>
 * 주문 생성 응답에 포함되는 주문 항목 DTO입니다.
 * 메뉴 스냅샷(이름/단가)과 수량, 라인 금액을 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@NoArgsConstructor
public class OrderItemResponse {
	private Long menuId;
	private String menuName;
	private Long unitPrice;
	private int quantity;
	private long lineAmount;

	public static OrderItemResponse from(CreateOrderItemResult item) {
		OrderItemResponse response = new OrderItemResponse();
		response.menuId = item.menuId();
		response.menuName = item.menuName();
		response.unitPrice = item.unitPrice();
		response.quantity = item.quantity();
		response.lineAmount = item.lineAmount();
		return response;
	}
}
