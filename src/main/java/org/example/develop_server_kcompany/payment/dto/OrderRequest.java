package org.example.develop_server_kcompany.payment.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 생성 요청 DTO입니다.
 * <p>
 * HTTP 요청 바디로부터 전달되는 값을 담는 객체로,
 * 컨트롤러 레이어에서 입력 형식 및 기본 제약 조건(@Valid)을 검증하는 역할을 합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@NoArgsConstructor
public class OrderRequest {

	@NotNull
	private Long userId;

	private String idempotencyKey;

	@NotEmpty
	@Valid
	private List<OrderItemRequest> items;

}

