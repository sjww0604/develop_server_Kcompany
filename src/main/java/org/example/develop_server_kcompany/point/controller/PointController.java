package org.example.develop_server_kcompany.point.controller;

import org.example.develop_server_kcompany.point.dto.PointChargeRequest;
import org.example.develop_server_kcompany.point.dto.PointChargeResponse;
import org.example.develop_server_kcompany.point.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 포인트 관련 API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 현재는 포인트 충전 엔드포인트를 제공하며, 요청 DTO는 {@link jakarta.validation.Valid}를 통해
 * 입력값을 검증한 뒤 {@link PointService}에 위임합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

	private final PointService pointService;

	/**
	 * 포인트를 충전합니다.
	 * <p>
	 * 요청 본문({@link PointChargeRequest})은 {@code @Valid}로 검증되며,
	 * 검증 실패 시 전역 예외 처리기에 의해 400 응답이 반환될 수 있습니다.
	 * </p>
	 *
	 * @param request 포인트 충전 요청 DTO
	 * @return 충전 결과(중복 여부, 충전 금액, 충전 후 잔액, 멱등성 키 등)
	 */
	@PostMapping("/charge")
	public ResponseEntity<PointChargeResponse> chargePoint(@Valid @RequestBody PointChargeRequest request) {
		return ResponseEntity.ok(pointService.charge(request));
	}
}