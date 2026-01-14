package org.example.develop_server_kcompany.order.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.example.develop_server_kcompany.common.exception.GlobalExceptionHandler;
import org.example.develop_server_kcompany.payment.controller.OrderController;
import org.example.develop_server_kcompany.payment.service.OrderService;
import org.example.develop_server_kcompany.payment.service.OrderService.CreateOrderItemResult;
import org.example.develop_server_kcompany.payment.service.OrderService.CreateOrderResult;
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
 * OrderControllerTest 테스트 클래스입니다.
 * <p>
 * 대상 메서드: 주문 생성 API
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 14.
 */
@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OrderService orderService;

	private static final String URL = "/api/orders";

	@Test
	@DisplayName("주문 생성 성공: 201 Created 및 응답 바디 반환")
	void createOrder_success_return201() throws Exception {
		// given
		CreateOrderResult result = new CreateOrderResult(
			1L,
			1L,
			3000L,
			7000L,
			List.of(new CreateOrderItemResult(10L, "라떼", 3000L, 1, 3000L)),
			"k1",
			false,
			LocalDateTime.parse("2026-01-14T10:00:00")
		);

		when(orderService.createOrder(eq(1L), any(), any())).thenReturn(result);

		Map<String, Object> request = new java.util.HashMap<String, Object>();
		request.put("userId", 1L);
		request.put("idempotencyKey", "k1");
		request.put("items", List.of(
			java.util.Map.of("menuId", 10L, "quantity", 1)
		));

		String body = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(post(URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.orderId").value(1))
			.andExpect(jsonPath("$.userId").value(1))
			.andExpect(jsonPath("$.totalAmount").value(3000))
			.andExpect(jsonPath("$.items[0].menuId").value(10));
	}

	@Test
	@DisplayName("요청 검증 실패: items가 비어 있으면 400 Bad Request")
	void createOrder_invalidRequest_itemsEmpty_returns400() throws Exception {
		// given
		Map<String, Object> request = new java.util.HashMap<String, Object>();
		request.put("userId", 1L);
		request.put("idempotencyKey", "k1");
		request.put("items", List.of());

		String body = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(post(URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest());

		verify(orderService, never()).createOrder(anyLong(), any(), any());
	}
}
