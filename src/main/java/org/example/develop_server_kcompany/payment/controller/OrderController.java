package org.example.develop_server_kcompany.payment.controller;

import static org.example.develop_server_kcompany.payment.service.OrderService.*;

import java.util.List;
import java.util.stream.Collectors;

import org.example.develop_server_kcompany.payment.dto.OrderRequest;
import org.example.develop_server_kcompany.payment.dto.OrderResponse;
import org.example.develop_server_kcompany.payment.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * OrderController 클래스입니다.
 * <p>
 * 주문 생성 API를 제공하는 컨트롤러입니다.
 * HTTP 요청(Request DTO)을 서비스 입력(Command)으로 변환하고,
 * 서비스 처리 결과를 응답(Response DTO)으로 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {

		Long userId = request.getUserId();

		List<CreateOrderItemCommand> commands = request.getItems().stream()
			.map(item -> new CreateOrderItemCommand(
				item.getMenuId(),
				item.getQuantity()
			))
			.collect(Collectors.toList());

		CreateOrderResult result = orderService.createOrder(
			userId,
			request.getIdempotencyKey(),
			commands
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(result));
	}
}
