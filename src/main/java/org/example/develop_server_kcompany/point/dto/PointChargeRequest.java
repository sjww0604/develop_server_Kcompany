package org.example.develop_server_kcompany.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PointChargeRequest 클래스입니다.
 * <p>
 * 사용자 식별자와 충전 포인트 금액을 기반으로 충전 요청을 진행합니다.
 * 멱등성 보장을 위해 idempotencyKey를 입력할 수 있습니다.(선택)
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Getter
@NoArgsConstructor
public class PointChargeRequest {

	@NotNull
	private Long userId;

	@NotNull
	@Positive
	private Long amount;

	private String idempotencyKey;
}