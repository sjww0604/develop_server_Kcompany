package org.example.develop_server_kcompany.common.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 공통 에러 응답을 표현하는 DTO 클래스입니다.
 * <p>
 * 전역 예외 처리({@link GlobalExceptionHandler})에서 사용되며,
 * 애플리케이션에서 발생한 예외 정보를 클라이언트에게
 * 일관된 JSON 형태로 전달하기 위한 목적을 가집니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Getter
@Builder
public class ApiErrorResponse {

	private final String code;
	private final String message;
	private final LocalDateTime timestamp;

	public static ApiErrorResponse from(ErrorCode errorCode) {
		return ApiErrorResponse.builder()
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.timestamp(LocalDateTime.now())
			.build();
	}
}