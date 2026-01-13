package org.example.develop_server_kcompany.user.domain;

import org.example.develop_server_kcompany.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User 클래스입니다.
 * <p>
 * 사용자 정보를 기본적으로 담고 있습니다.
 * 프로젝트 진행 간에는 별도 인증/인가 로직은 없이 초기 데이터 세팅으로 진행합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	public User(String name) {
		this.name = name;
	}
}
