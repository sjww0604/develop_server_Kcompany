package org.example.develop_server_kcompany.payment.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OrderPaidEventItemPayload 클래스입니다.
 * <p>
 * TODO: 클래스의 역할을 작성하세요.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEventItemPayload {

	private Long menuId;
	private int quantity;
	private Long unitPriceSnapshot;
	private Long lineAmount;

}