package org.example.develop_server_kcompany.payment.messaging.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 결제 완료 이벤트를 Kafka로 전송하기 위한 Payload DTO 입니다.
 * <p>
 * 본 클래스는 외부 데이터 수집 플랫폼으로 전달되는 이벤트 전용 객체이며,
 * 비즈니스 로직을 포함하지 않습니다.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEventPayload {

	private Long orderId;
	private Long userId;
	private Long totalAmount;
	private String paidAt;
	private List<OrderPaidEventItemPayload> items;

}