package org.example.develop_server_kcompany.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.develop_server_kcompany.menu.dto.MenuListResponse;
import org.example.develop_server_kcompany.menu.repository.MenuRepository;

/**
 * 메뉴 관련 전용 서비스입니다.
 * <p>
 * 메뉴 목록 조회 API에서 필요한 조회 로직을 담당하며,
 * 엔티티를 직접 반환하지 않고 DTO로 변환하여 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 12.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

	private final MenuRepository menuRepository;

	/**
	 * 메뉴 목록 조회 메서드입니다.
	 * @return menuList[]
	 */
	@Transactional(readOnly = true)
	public List<MenuListResponse> getMenus() {
		List<MenuListResponse> menus = menuRepository.findAllByIsActiveTrue()
			.stream()
			.map(MenuListResponse::from)
			.toList();

		log.info("[MenuService] 판매중인 메뉴 목록 조회 성공 - count={}", menus.size());
		return menus;
	}
}