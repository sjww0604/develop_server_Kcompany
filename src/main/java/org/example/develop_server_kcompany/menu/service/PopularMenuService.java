package org.example.develop_server_kcompany.menu.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.develop_server_kcompany.menu.domain.Menu;
import org.example.develop_server_kcompany.menu.repository.MenuRepository;
import org.example.develop_server_kcompany.menu.dto.PopularMenuResponse;
import org.example.develop_server_kcompany.payment.messaging.PopularityRedisRepository;
import org.example.develop_server_kcompany.payment.messaging.model.MenuScore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * PopularMenuService 클래스입니다.
 * <p>
 * 최근 7일간의 판매량(수량 누적)을 기준으로 인기 메뉴 TOP3를 조회합니다.
 * </p>
 * <p>
 * Redis에 일자별로 누적된 ZSET(popular:menu:{yyyyMMdd})을 합산하여 TOP3를 조회합니다.
 * 조회된 menuId 목록으로 Menu를 조회한 뒤, 응답 DTO로 변환합니다.
 * 날짜 계산은 Clock을 사용하여 테스트에서 시간을 고정할 수 있게 합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Service
@RequiredArgsConstructor
public class PopularMenuService {

	private static final String KEY_PREFIX = "popular:menu:";
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final PopularityRedisRepository popularityRedisRepository;
	private final MenuRepository menuRepository;
	private final Clock clock;

	/**
	 * 최근 7일 인기 메뉴 TOP3를 조회합니다.
	 *
	 * @return 인기 메뉴 응답 목록(판매량 내림차순, 최대 3개)
	 */
	public List<PopularMenuResponse> getTop3() {
		List<String> dailyKeys = buildLastDaysKeys(7);

		List<MenuScore> topMenus = popularityRedisRepository.findTopMenusForLastDays(dailyKeys, 3);
		if (topMenus.isEmpty()) {
			return List.of();
		}

		List<Long> menuIds = topMenus.stream()
			.map(MenuScore::getMenuId)
			.toList();

		Map<Long, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
			.filter(Menu::isActive) // 판매중인 메뉴만
			.collect(Collectors.toMap(Menu::getId, Function.identity()));

		// Redis 순위(판매량 내림차순) 그대로 유지해서 응답 생성
		List<PopularMenuResponse> result = new ArrayList<>(3);
		for (MenuScore ms : topMenus) {
			Menu menu = menuMap.get(ms.getMenuId());
			if (menu == null) {
				continue;
			}
			result.add(PopularMenuResponse.from(menu, ms.getCount()));
		}

		// 혹시 모를 순서 불안 방지(안전망)
		result.sort(Comparator.comparingLong(PopularMenuResponse::getOrderCount).reversed());

		return result;
	}

	private List<String> buildLastDaysKeys(int days) {
		LocalDate today = LocalDate.now(clock);

		List<String> keys = new ArrayList<>(days);
		for (int i = 0; i < days; i++) {
			String yyyyMMdd = today.minusDays(i).format(DAY_FORMATTER);
			keys.add(KEY_PREFIX + yyyyMMdd);
		}
		return keys;
	}

}