package org.example.develop_server_kcompany.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 목록 요청 DTO 입니다.
 * <p>
 * 주문 생성 요청에 포함되는 개별 상품 정보를 표현합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@NoArgsConstructor
public class OrderItemRequest {

	@NotNull
	@Positive
	private Long menuId;

	@Positive
	private int quantity;

}