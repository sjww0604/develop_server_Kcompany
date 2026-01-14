package org.example.develop_server_kcompany.menu.controller;

import java.util.List;

import org.example.develop_server_kcompany.menu.dto.PopularMenuResponse;
import org.example.develop_server_kcompany.menu.service.PopularMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * PopularMenuController 클래스입니다.
 * <p>
 * TODO: 클래스의 역할을 작성하세요.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus/ranking")
public class PopularMenuController {

	private final PopularMenuService popularMenuService;

	/**
	 * 최근 7일 인기 메뉴 TOP3를 조회합니다.
	 *
	 * @return 인기 메뉴 목록(최대 3개, 판매량 내림차순)
	 */
	@GetMapping
	public List<PopularMenuResponse> getPopularMenus() {
		return popularMenuService.getTop3();
	}

}