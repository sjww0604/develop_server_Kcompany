package org.example.develop_server_kcompany.payment.messaging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Redis 인기 메뉴 집계 조회 결과를 표현하는 내부 모델입니다.
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@AllArgsConstructor
public class MenuScore {

	private Long menuId;
	private long count;

}