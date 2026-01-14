package org.example.develop_server_kcompany.payment.messaging;

import org.example.develop_server_kcompany.payment.messaging.dto.OrderPaidEventPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * PaymentEventProducer 클래스입니다.
 * <p>
 * 결제 완료 이후 생성된 이벤트 Payload를 Kafka 토픽으로 전송하는 역할을 담당합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	@Value("${app.kafka.topic.payment}")
	private String paymentTopic;

	/**
	 * 결제 완료 이벤트를 Kafka로 전송합니다.
	 *
	 * @param payload 결제 완료 이벤트 Payload
	 */
	public void sendOrderCompletedEvent(OrderPaidEventPayload payload) {
		String key = String.valueOf(payload.getOrderId());
		kafkaTemplate.send(paymentTopic, key, payload);
	}
}