package org.example.develop_server_kcompany.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리를 담당하는 핸들러입니다.
 * <p>
 * 애플리케이션 전반에서 발생하는 예외를 한 곳에서 처리하여,
 * 클라이언트에게 일관된 에러 응답({@link ApiErrorResponse})을 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 애플리케이션에서 정의한 {@link CustomException}을 처리합니다.
	 *
	 * @param e 발생한 커스텀 예외
	 * @return {@link ErrorCode}에 매핑된 HTTP 상태 코드 및 에러 응답
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiErrorResponse.from(errorCode));
	}

	/**
	 * 요청 값 검증(Bean Validation) 실패 시 발생하는 예외를 처리합니다.
	 *
	 * @param e 검증 실패 예외
	 * @return 400(BAD_REQUEST) 에러 응답
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
		return ResponseEntity
			.status(ErrorCode.INVALID_REQUEST.getStatus())
			.body(ApiErrorResponse.from(ErrorCode.INVALID_REQUEST));
	}

	/**
	 * 그 외 처리되지 않은 모든 예외를 처리합니다.
	 * <p>
	 * 예상하지 못한 예외는 서버 내부 오류(500)로 응답하며, 원인 파악을 위해 로그를 남깁니다.
	 * </p>
	 *
	 * @param e 처리되지 않은 예외
	 * @return 500(INTERNAL_SERVER_ERROR) 에러 응답
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleException(Exception e) {
		log.error("[정의한 예외처리 외의 실패 케이스 발생]", e);
		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
			.body(ApiErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}