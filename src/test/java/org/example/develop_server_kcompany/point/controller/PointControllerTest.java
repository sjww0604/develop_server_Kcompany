package org.example.develop_server_kcompany.point.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.example.develop_server_kcompany.common.exception.GlobalExceptionHandler;
import org.example.develop_server_kcompany.point.dto.PointChargeRequest;
import org.example.develop_server_kcompany.point.dto.PointChargeResponse;
import org.example.develop_server_kcompany.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * PointController에 대한 Web MVC 테스트 클래스입니다.
 * <p>
 * {@code @WebMvcTest}로 컨트롤러 계층만 로드하여 요청/응답 및 입력 검증(@Valid) 동작을 검증합니다.
 * 서비스 계층은 {@code @MockBean}으로 대체합니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 13.
 */
@WebMvcTest(PointController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class PointControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PointService pointService;

	@Test
	@DisplayName("포인트 충전 성공 케이스 검증")
	void charge_success_returnOkTest() throws Exception {
		// given
		Map<String, Object> request = new HashMap<>();
		request.put("userId", 1L);
		request.put("amount", 1000L);
		request.put("idempotencyKey", "test-key");

		mockMvc.perform(post("/api/point/charge")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		PointChargeResponse response = PointChargeResponse.of(
			false, 1L, 1_000L, 1_000L, "test-key");
		when(pointService.charge(any(PointChargeRequest.class))).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/point/charge")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("포인트 충전 실패 케이스 검증")
	void charge_invalidRequest_returnBadRequestTest() throws Exception {
		// given: amount 누락으로 @Valid 검증 실패 유도
		String invalidJson = "{\"userId\":1}";

		// when & then
		mockMvc.perform(post("/api/point/charge")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson))
			.andExpect(status().isBadRequest());
	}
}
