package org.example.develop_server_kcompany.menu.dto;

import org.example.develop_server_kcompany.menu.domain.Menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PopularMenuResponse 클래스입니다.
 * <p>
 * 최근 7일 판매량 기준 인기 메뉴 조회 응답 DTO입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Getter
@AllArgsConstructor
public class PopularMenuResponse {

	private Long menuId;
	private String menuName;
	private Integer price;
	private long orderCount;

	public static PopularMenuResponse from(Menu menu, long orderCount) {
		return new PopularMenuResponse(
			menu.getId(),
			menu.getName(),
			menu.getPrice(),
			orderCount
		);
	}
}