package org.example.develop_server_kcompany.point.domain;

import org.example.develop_server_kcompany.common.entity.BaseTimeEntity;
import org.example.develop_server_kcompany.point.enums.PointTransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 증감 이력을 기록하는 엔티티입니다.
 * <p>
 * 포인트 충전/사용과 같은 모든 변경 내역은 이 테이블에 기록되며,
 * 잔액(balanceAfter)은 해당 트랜잭션 처리 이후의 스냅샷 값을 의미합니다.
 * 멱등성 처리를 위해 idempotencyKey를 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Entity
@Table(
	name = "point_transactions",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_point_tx_user_idempotency",
			columnNames = {"user_id", "idempotency_key"}
		)
	})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	private PointTransactionType type;

	@Column(nullable = false)
	private Long amount;

	@Column(nullable = false, name = "balance_after")
	private Long balanceAfter;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "idempotency_key", nullable = false, length = 100)
	private String idempotencyKey;

	/**
	 *
	 * @param userId         충전/사용 대상 사용자 식별자
	 * @param type           트랜잭션 타입 (예: CHARGE / SPEND)
	 * @param amount         증감 금액
	 * @param balanceAfter   처리 이후 잔액 스냅샷
	 * @param idempotencyKey 멱등성 키
	 * @param orderId        주문 연계용 주문 ID (충전인 경우 null 가능)
	 */
	public PointTransaction(
		Long userId, PointTransactionType type, Long amount, Long balanceAfter, Long orderId, String idempotencyKey) {
		this.userId = userId;
		this.type = type;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.orderId = orderId;
		this.idempotencyKey = idempotencyKey;
	}
}