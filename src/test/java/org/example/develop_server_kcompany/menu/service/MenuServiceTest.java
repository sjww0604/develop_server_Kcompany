package org.example.develop_server_kcompany.menu.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.example.develop_server_kcompany.menu.domain.Menu;
import org.example.develop_server_kcompany.menu.dto.MenuListResponse;
import org.example.develop_server_kcompany.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@link MenuService} 단위(서비스 레이어) 테스트입니다.
 * <p>
 * 메뉴 목록 조회 기능에서 <b>판매중(isActive=true)</b>인 메뉴만 조회되는지 검증합니다.
 * 테스트는 H2(in-memory) 환경에서 동작하며, 각 테스트는 독립적인 데이터 셋업을 수행합니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 13.
 */
@SpringBootTest
@ActiveProfiles("test")
public class MenuServiceTest {

	@Autowired
	private MenuRepository menuRepository;


	@Autowired
	private MenuService menuService;

	@BeforeEach
	void setUp() {
		menuRepository.save(new Menu("아메리카노", 4000, true));
		menuRepository.save(new Menu("카페라떼", 4500, true));
		menuRepository.save(new Menu("모카", 5000, false)); // 판매중 아님
	}

	@Test
	@DisplayName("판매중 메뉴 목록 조회 테스트")
	void getMenus_returnsOnlyActiveMenusTest() {
		// when
		List<MenuListResponse> result = menuService.getMenus();

		// then
		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting("name")
			.containsExactlyInAnyOrder("아메리카노", "카페라떼");

	}
}