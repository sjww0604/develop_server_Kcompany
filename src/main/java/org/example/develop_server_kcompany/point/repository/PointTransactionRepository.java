package org.example.develop_server_kcompany.point.repository;

import java.util.Optional;

import org.example.develop_server_kcompany.point.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PointTransaction 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스입니다.
 * <p>
 * 포인트 충전 및 사용과 같이 포인트 잔액에 변동이 발생하는 모든 이력을 저장하고 조회하는 역할을 수행합니다.
 * 특히 멱등성(idempotency) 처리를 위해 사용자 ID와 멱등성 키(idempotencyKey)를 기준으로
 * 이미 처리된 트랜잭션이 존재하는지 조회하는 기능을 제공합니다.
 * 이를 통해 중복 요청으로 인한 포인트 중복 반영을 방지합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

	Optional<PointTransaction> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

}