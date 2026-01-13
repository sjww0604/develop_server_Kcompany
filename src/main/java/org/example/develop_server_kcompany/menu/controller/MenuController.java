package org.example.develop_server_kcompany.menu.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.example.develop_server_kcompany.menu.dto.MenuListResponse;
import org.example.develop_server_kcompany.menu.service.MenuService;

/**
 * 메뉴 관련 API를 제공하는 컨트롤러입니다.
 * <p>
 * 메뉴 목록 조회 등 메뉴 도메인에 대한 HTTP 요청을 처리하고,
 * 서비스 계층에서 반환한 DTO를 응답으로 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 12.
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	@GetMapping
	public ResponseEntity<List<MenuListResponse>> coffeeMenuList() {
		return ResponseEntity.ok(menuService.getMenus());
	}
}