package org.example.develop_server_kcompany.common.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 연결 및 기본 템플릿을 설정하는 인프라 설정 클래스입니다.
 * <p>
 * Spring Data Redis가 제공하는 {@link RedisConnectionFactory}를 기반으로
 * {@link StringRedisTemplate}를 Bean으로 등록하여 문자열 기반 Key-Value 접근을 공통적으로 사용할 수 있도록 합니다.
 * </p>
 *
 * <p>
 * 본 설정은 캐시, 멱등성 키 관리, 간단한 상태 저장 등 인프라 레벨의 Redis 활용을 위한 기본 골격 역할을 합니다.
 * </p>
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Configuration
public class RedisConfig {

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}
}