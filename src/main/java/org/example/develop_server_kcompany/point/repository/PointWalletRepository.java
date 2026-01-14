package org.example.develop_server_kcompany.point.repository;

import org.example.develop_server_kcompany.point.domain.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PointWalletRepository 인터페이스입니다.
 * <p>
 * 사용자별 포인트 지갑(PointWallet)을 조회 및 저장하기 위한 역할을 하며,
 * 기본적인 CRUD 기능은 {@link JpaRepository}를 통해 제공받습니다.
 * 본 Repository는 포인트 충전/사용 시 현재 잔액 상태를 조회하거나, 사용자의 포인트 지갑이 존재하지 않는 경우 신규 생성하는 용도로 사용됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {

}