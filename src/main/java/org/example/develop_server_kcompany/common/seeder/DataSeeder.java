package org.example.develop_server_kcompany.common.seeder;

import java.util.List;

import org.example.develop_server_kcompany.menu.domain.Menu;
import org.example.develop_server_kcompany.menu.repository.MenuRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 실행 시 초기 더미 데이터를 적재하는 클래스입니다.
 * <p>
 * 별도의 등록 API를 구현하지 않고, 애플리케이션 실행 시 커피 메뉴 데이터를 자동으로 생성합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final MenuRepository menuRepository;

	@Override
	@Transactional
	public void run(String... args) {
		if (menuRepository.count() > 0) {
			return;
		}

		List<Menu> menus = List.of(
			new Menu("아메리카노", 4000, true),
			new Menu("카페라떼", 4500, true),
			new Menu("바닐라라떼", 5000, true),
			new Menu("오곡라떼", 5000, true),
			new Menu("유자차", 5500, true),
			new Menu("딸기스무디", 7000, true)
		);

		menuRepository.saveAll(menus);
	}



}