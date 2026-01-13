package org.example.develop_server_kcompany.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 전역에서 발생할 수 있는 오류 상황을 정의한 Enum 클래스입니다.
 * <p>
 * 각 ErrorCode는 HTTP 상태코드 {@link HttpStatus}, 서비스 고유 에러코드 문자열, 클라이언트에게 전달될 메시지를 포함합니다.
 * GlobalExceptionHandler에서 발생한 예외를 일관된 응답 형식으로 변환할 때 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 오류가 발생했습니다."),

	// 메뉴
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M404", "메뉴를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}