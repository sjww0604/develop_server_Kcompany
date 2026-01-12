package org.example.develop_server_kcompany.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.example.develop_server_kcompany.menu.domain.Menu;

/**
 * Menu 엔티티에 대한 데이터 접근(조회/저장)을 담당하는 Repository 인터페이스입니다.
 * <p>
 * 커피 메뉴 목록 조회 API에서 활성화된 메뉴만 조회하기 위해
 * {@link #findAllByIsActiveTrue()} 쿼리를 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 12.
 */
public interface MenuRepository extends JpaRepository<Menu, Long> {

	List<Menu> findAllByIsActiveTrue();

}