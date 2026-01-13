package org.example.develop_server_kcompany.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 포인트 충전 요청에 대한 처리 결과를 전달하는 응답 DTO입니다.
 * <p>
 * 멱등성 처리가 적용되어 동일한 요청이 재전송된 경우,
 * duplicate 필드를 통해 이미 처리된 요청인지 여부를 명확히 구분할 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Getter
@AllArgsConstructor
public class PointChargeResponse {

	private boolean success;
	private boolean duplicate;
	private Long userId;
	private Long chargedAmount;
	private Long balanceAfter;
	private String idempotencyKey;

	public static PointChargeResponse of(
		boolean duplicate, Long userId, Long amount, Long balanceAfter, String key) {
		return new PointChargeResponse(true, duplicate, userId, amount, balanceAfter, key);
	}
}