package org.example.develop_server_kcompany.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.develop_server_kcompany.menu.domain.Menu;

/**
 * 메뉴 목록 조회 API의 응답 DTO 클래스입니다.
 * <p>
 * 외부로 노출될 필드만 담기 위해 엔티티를 직접 반환하지 않고
 * 메뉴 식별자, 이름, 가격 정보를 전달합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 12.
 */
@Getter
@AllArgsConstructor
public class MenuListResponse {

	private final Long id;
	private final String name;
	private final Integer price;

	/**
	 * Menu 엔티티를 메뉴 목록 조회 응답 DTO로 변환합니다.
	 *
	 * @param menu 변환 대상 메뉴 엔티티
	 * @return 메뉴 목록 조회 응답 DTO
	 */
	public static MenuListResponse from(Menu menu) {
		return new MenuListResponse(menu.getId(), menu.getName(), menu.getPrice());
	}
}