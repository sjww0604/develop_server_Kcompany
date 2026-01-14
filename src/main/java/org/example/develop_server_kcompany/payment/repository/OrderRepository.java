package org.example.develop_server_kcompany.payment.repository;

import java.util.Optional;

import org.example.develop_server_kcompany.payment.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문(Order) 엔티티에 대한 영속성 처리를 담당하는 Repository 인터페이스입니다.
 *
 * <p>
 * 주문 생성 시 멱등성(Idempotency)을 보장하기 위해,
 * {@code userId + idempotencyKey} 조합을 기준으로 주문을 조회하는 기능을 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
}