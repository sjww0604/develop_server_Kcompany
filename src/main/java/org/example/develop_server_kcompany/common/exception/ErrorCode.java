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
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

	// 사용자
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "유저를 조회할 수 없습니다."),
	// 메뉴
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다."),

	//포인트
	WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", "지갑 정보를 조회할 수 없습니다.(오류)"),
	CHARGE_FAIL(HttpStatus.EXPECTATION_FAILED, "CHARGE_FAIL", "포인트 충전 요청이 실패했습니다."),
	SPEND_FAIL(HttpStatus.EXPECTATION_FAILED, "SPEND_FAIL", "포인트 차감 요청이 실패했습니다."),
	INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "충전/차감 금액은 양수만 입력 가능합니다."),
	BALANCE_OVERFLOW(HttpStatus.BAD_REQUEST, "BALANCE_OVERFLOW", "최대 보유 가능 포인트를 초과합니다."),
	INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "INSUFFICIENT_BALANCE", "포인트 잔액이 부족합니다. 충전 후 재구매 해주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}