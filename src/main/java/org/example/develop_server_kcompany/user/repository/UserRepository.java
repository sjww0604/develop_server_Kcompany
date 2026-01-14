package org.example.develop_server_kcompany.user.repository;

import org.example.develop_server_kcompany.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserRepository 인터페이스입니다.
 * <p>
 * TODO: 인터페이스의 역할을 작성하세요.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
public interface UserRepository extends JpaRepository<User, Long> {

}