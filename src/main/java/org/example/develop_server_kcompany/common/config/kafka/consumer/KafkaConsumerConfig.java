package org.example.develop_server_kcompany.common.config.kafka.consumer;

import org.example.develop_server_kcompany.payment.messaging.dto.OrderPaidEventPayload;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Map;

/**
 * KafkaConsumerConfig 클래스입니다.
 * <p>
 * Kafka 메시지 소비(Consumer)를 위한 기본 설정(Base Configuration) 클래스입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Configuration
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
		return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object>
	kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, Object> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}

	/**
	 * 주문 결제 완료 이벤트를 DTO로 역직렬화하여 소비하기 위한 ConsumerFactory 입니다.
	 * <p>
	 * Producer가 JSON 형태로 전송하는 값을 JsonDeserializer로 DTO로 변환합니다.
	 * </p>
	 */
	@Bean
	public ConsumerFactory<String, OrderPaidEventPayload> orderPaidEventConsumerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		JsonDeserializer<OrderPaidEventPayload> deserializer = new JsonDeserializer<>(OrderPaidEventPayload.class);
		deserializer.addTrustedPackages("*");
		deserializer.setUseTypeHeaders(false);

		return new DefaultKafkaConsumerFactory<>(
			props,
			new StringDeserializer(),
			deserializer
		);
	}

	/**
	 * 주문 결제 완료 이벤트(OrderPaidEventPayload)를 DTO로 소비하는 ListenerContainerFactory 입니다.
	 */
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, OrderPaidEventPayload> orderPaidEventKafkaListenerContainerFactory(
		ConsumerFactory<String, OrderPaidEventPayload> orderPaidEventConsumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<String, OrderPaidEventPayload> factory =
			new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(orderPaidEventConsumerFactory);
		factory.setConcurrency(3);
		return factory;
	}
}