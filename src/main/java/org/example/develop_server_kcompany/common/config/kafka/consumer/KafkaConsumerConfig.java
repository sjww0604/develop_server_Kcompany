package org.example.develop_server_kcompany.common.config.kafka.consumer;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

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
}