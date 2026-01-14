package org.example.develop_server_kcompany.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clock Bean 설정 클래스입니다.
 * <p>
 * 날짜/시간 의존 로직(예: 최근 7일 키 생성)을 테스트에서 고정하기 위해 Clock을 Bean으로 등록합니다.
 * </p>
 */
@Configuration
public class ClockConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}