package org.example.develop_server_kcompany.payment.messaging;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.example.develop_server_kcompany.payment.messaging.model.MenuScore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * PopularityRedisRepository 클래스입니다.
 * <p>
 * 인기 메뉴 집계를 Redis ZSET 구조로 관리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Repository
@RequiredArgsConstructor
public class PopularityRedisRepository {

	private static final Duration DAILY_KEY_TTL = Duration.ofDays(9);

	private final StringRedisTemplate stringRedisTemplate;

	/**
	 * 일자별 인기 메뉴 판매량을 누적합니다.
	 * <p>
	 * Redis ZSET 구조를 사용하여 menuId를 member로, 판매 수량을 score로 관리합니다.
	 * </p>
	 *
	 * @param redisKey popular:menu:{yyyyMMdd}
	 * @param menuId   메뉴 식별자
	 * @param quantity 누적할 판매 수량 (양수)
	 */
	public void incrementDailyMenuCount(String redisKey, Long menuId, int quantity) {
		stringRedisTemplate.opsForZSet()
			.incrementScore(redisKey, menuId.toString(), quantity);

		stringRedisTemplate.expire(redisKey, DAILY_KEY_TTL);
	}

	/**
	 * 최근 N일(일자별 ZSET 키 목록) 집계를 합산하여 상위 메뉴를 조회합니다.
	 * <p>
	 * 일자별 ZSET(예: popular:menu:20260114, popular:menu:20260113 ...)을
	 * Redis {@code ZUNIONSTORE}로 합산한 뒤, 합산 결과에서 {@code ZREVRANGE WITHSCORES}로
	 * 상위 {@code limit}개를 조회합니다.
	 * </p>
	 * <p>
	 * 합산 결과는 임시 키(tmpKey)에 저장되며, 조회 목적의 데이터이므로 짧은 TTL(기본 30초)을 설정합니다.
	 * </p>
	 *
	 * @param dailyKeys 최근 N일치 일자별 인기 집계 ZSET 키 목록(최신 날짜 키가 첫 번째 권장)
	 * @param limit    조회할 상위 개수(예: TOP3이면 3)
	 * @return 판매량(count) 기준 내림차순 정렬된 메뉴 집계 목록(menuId, count)
	 */
	public List<MenuScore> findTopMenusForLastDays(List<String> dailyKeys, int limit) {
		if (dailyKeys == null || dailyKeys.isEmpty() || limit <= 0) {
			return List.of();
		}

		String firstKey = dailyKeys.get(0);
		List<String> otherKeys = dailyKeys.subList(1, dailyKeys.size());

		// 7일치 합산 결과를 임시 키로 저장
		String tmpKey = "popular:menu:tmp:" + System.currentTimeMillis();

		stringRedisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, tmpKey);

		stringRedisTemplate.expire(tmpKey, Duration.ofSeconds(30));

		Set<ZSetOperations.TypedTuple<String>> tuples =
			stringRedisTemplate.opsForZSet().reverseRangeWithScores(tmpKey, 0, limit - 1);

		if (tuples == null || tuples.isEmpty()) {
			return List.of();
		}

		return tuples.stream()
			.filter(t -> t.getValue() != null && t.getScore() != null)
			.map(t -> new MenuScore(Long.valueOf(t.getValue()), t.getScore().longValue()))
			.toList();
	}
}
