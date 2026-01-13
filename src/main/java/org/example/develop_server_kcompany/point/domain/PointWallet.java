package org.example.develop_server_kcompany.point.domain;

import org.example.develop_server_kcompany.common.entity.BaseTimeEntity;
import org.example.develop_server_kcompany.common.exception.CustomException;
import org.example.develop_server_kcompany.common.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자의 현재 포인트 잔액을 보관하는 지갑(월렛) 엔티티입니다.
 * <p>
 * 사용자 1명당 1개의 지갑을 가지며, 잔액(balance)은 현재 보유 포인트를 의미합니다.
 * 동시성 제어를 위해 낙관적 락(@Version)을 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Entity
@Table(name = "point_wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWallet extends BaseTimeEntity {

	@Id
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long balance = 0L;

	@Version
	@Column(nullable = false)
	private Integer version = 0;

	public PointWallet(Long userId) {
		this.userId = userId;
		this.balance = 0L;
	}

	/**
	 * 포인트를 충전합니다.
	 * @param amount 충전 금액(양수)
	 * @return 충전 이후 잔액(balanceAfter)
	 */
	public long charge(long amount) {
		if (amount <= 0) {
			throw new CustomException(ErrorCode.INVALID_AMOUNT);
		}
		this.balance += amount;
		return this.balance;
	}

	/**
	 * 포인트를 사용(차감)합니다.
	 * @param amount 사용 금액(양수)
	 * @return 사용 이후 잔액(balanceAfter)
	 */
	public long spend(long amount) {
		if (amount <= 0) {
			throw new CustomException(ErrorCode.INVALID_AMOUNT);
		}
		if (this.balance < amount) {
			throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
		}
		this.balance -= amount;
		return this.balance;
	}



}