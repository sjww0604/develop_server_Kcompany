package org.example.develop_server_kcompany.menu.domain;

import org.example.develop_server_kcompany.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Menu 클래스입니다.
 * <p>
 * 커피 메뉴를 담는 객체 클래스입니다.
 * 모든 컬럼값은 공백일 수 없으며, 판매가능여부는 기본적으로 판매가능(true)로 설정합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 12.
 */
@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor
public class Menu extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false, name = "is_active")
	private boolean isActive = true;

	public Menu(String name, Integer price, boolean isActive) {
		this.name = name;
		this.price = price;
		this.isActive = isActive;
	}
}