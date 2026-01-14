package org.example.develop_server_kcompany.point.service;

import java.util.UUID;

import org.example.develop_server_kcompany.common.exception.CustomException;
import org.example.develop_server_kcompany.common.exception.ErrorCode;
import org.example.develop_server_kcompany.point.domain.PointTransaction;
import org.example.develop_server_kcompany.point.domain.PointWallet;
import org.example.develop_server_kcompany.point.dto.PointChargeRequest;
import org.example.develop_server_kcompany.point.dto.PointChargeResponse;
import org.example.develop_server_kcompany.point.enums.PointTransactionType;
import org.example.develop_server_kcompany.point.repository.PointTransactionRepository;
import org.example.develop_server_kcompany.point.repository.PointWalletRepository;
import org.example.develop_server_kcompany.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PointService 클래스입니다.
 * <p>
 * 포인트 충전과 같은 포인트 유스케이스를 처리하는 서비스 클래스입니다.
 * 멱등성 키를 기준으로 중복 요청을 방지하고, 잔액 갱신과 원장 기록을 트랜잭션으로 처리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 13.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointService {

	private final UserRepository userRepository;
	private final PointWalletRepository pointWalletRepository;
	private final PointTransactionRepository pointTransactionRepository;
	private final TransactionTemplate transactionTemplate;

	/**
	 * 포인트 충전 유스케이스입니다.
	 * 멱등성 키 기준으로 중복 충전을 방지합니다.
	 */
	public PointChargeResponse charge(PointChargeRequest request) {
		validateChargeUseCase(request);

		Long userId = request.getUserId();
		Long amount = request.getAmount();
		String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());

		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				return transactionTemplate.execute(status -> pointTransactionRepository
					.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
					.map(tx -> {
						log.info("[POINT] 이미 처리된 요청입니다. userId={}, amount={}, key={}",
							userId, tx.getAmount(), idempotencyKey);
						return PointChargeResponse.of(
							true, userId, tx.getAmount(), tx.getBalanceAfter(), idempotencyKey);
					})
					.orElseGet(() -> {
						validateUserExists(userId);

						PointWallet pointWallet = pointWalletRepository.findById(userId)
							.orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

						long balanceAfter = pointWallet.charge(amount);

						PointTransaction tx = new PointTransaction(
							userId,
							PointTransactionType.CHARGE,
							amount,
							balanceAfter,
							null,
							idempotencyKey
						);

						try {
							pointTransactionRepository.save(tx);
						} catch (DataIntegrityViolationException e) {
							return pointTransactionRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
								.map(dupTx -> {
									log.info("[POINT] 중복 요청 처리 감지. userId={}, key={}", userId, idempotencyKey);
									return PointChargeResponse.of(true, userId, dupTx.getAmount(),
										dupTx.getBalanceAfter(), idempotencyKey);
								})
								.orElseThrow(() -> e);
						}

						log.info("[POINT] 포인트 충전 성공. userId={}, amount={}, balanceAfter={}, key={}",
							userId, amount, balanceAfter, idempotencyKey);
						return PointChargeResponse.of(false, userId, amount, balanceAfter, idempotencyKey);
					}));
			} catch (ObjectOptimisticLockingFailureException e) {
				log.warn("[POINT] 낙관적 락 충돌 재시도. attempt={}, userId={}, key={}", attempt, userId, idempotencyKey);
				if (attempt == 3) {
					log.error("[POINT] 포인트 충전 실패(낙관적 락 충돌). userId={}, amount={}, key={}, attempts={}",
						userId, amount, idempotencyKey, attempt, e);
					throw new CustomException(ErrorCode.CHARGE_FAIL);
				}

				long delayMs = 100L * (1L << (attempt - 1));
				try {
					Thread.sleep(delayMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					log.error("[POINT] BackOff 대기 중 인터럽트 발생. userId={}, key={}", userId, idempotencyKey, ie);
					throw new CustomException(ErrorCode.CHARGE_FAIL);
				}
			}
		}
		throw new CustomException(ErrorCode.CHARGE_FAIL);
	}

	/**
	 * 주문 등 외부 유스케이스에서 포인트를 "사용(차감)" 처리하는 메서드입니다.
	 * <p>
	 * 멱등성 키(idempotencyKey)를 기준으로 중복 차감을 방지하며,
	 * 지갑 잔액 갱신 + 거래 이력 기록을 하나의 트랜잭션으로 처리합니다.
	 * </p>
	 *
	 * <p>
	 * 멱등성 키는 optional-or-empty가 가능하므로, 비어 있는 경우에는 서버에서 UUID를 생성하여 사용합니다.
	 * (단, 응답을 못 받은 실패/타임아웃 상황에서는 동일 키를 재전달할 수 없어 완전한 멱등 재시도는 제한됩니다.)
	 * </p>
	 *
	 * @param userId         사용자 식별자
	 * @param amount         차감할 금액(양수)
	 * @param idempotencyKey 멱등성 키(선택)
	 * @return 차감 처리 결과(중복 여부, 잔액, 최종 멱등성 키)
	 */
	public SpendResult spend(Long userId, long amount, String idempotencyKey) {
		validateSpendUseCase(userId, amount);

		String normalizedKey = normalizeIdempotencyKey(idempotencyKey);

		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				SpendResult result = transactionTemplate.execute(status -> pointTransactionRepository
					.findByUserIdAndIdempotencyKey(userId, normalizedKey)
					.map(tx -> {
						log.info("[POINT] 이미 처리된 사용(차감) 요청입니다. userId={}, amount={}, key={}",
							userId, amount, normalizedKey);
						return SpendResult.of(true, tx.getBalanceAfter(), normalizedKey);
					})
					.orElseGet(() -> {
						validateUserExists(userId);

						PointWallet pointWallet = pointWalletRepository.findById(userId)
							.orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

						long balanceAfter = pointWallet.spend(amount);

						PointTransaction tx = new PointTransaction(
							userId,
							PointTransactionType.SPEND,
							amount,
							balanceAfter,
							null,
							normalizedKey
						);

						try {
							pointTransactionRepository.save(tx);
						} catch (DataIntegrityViolationException e) {
							// 동시 요청으로 동일 키 트랜잭션이 이미 저장된 경우:
							// 지갑 차감만 남지 않도록 현재 트랜잭션을 롤백시키고, 기존 기록을 조회하여 결과로 반환한다.
							status.setRollbackOnly();

							return pointTransactionRepository.findByUserIdAndIdempotencyKey(userId, normalizedKey)
								.map(dupTx -> {
									log.info("[POINT] 중복 사용(차감) 요청 처리 감지. userId={}, key={}",
										userId, normalizedKey);
									return SpendResult.of(true, dupTx.getBalanceAfter(), normalizedKey);
								})
								.orElseThrow(() -> e);
						}

						log.info("[POINT] 포인트 사용(차감) 성공. userId={}, amount={}, balanceAfter={}, key={}",
							userId, amount, balanceAfter, normalizedKey);
						return SpendResult.of(false, balanceAfter, normalizedKey);
					}));

				return result;
			} catch (ObjectOptimisticLockingFailureException e) {
				log.warn("[POINT] 낙관적 락 충돌 재시도(사용/차감). attempt={}, userId={}, key={}",
					attempt, userId, normalizedKey);

				if (attempt == 3) {
					log.error("[POINT] 포인트 사용(차감) 실패(낙관적 락 충돌). userId={}, amount={}, key={}, attempts={}",
						userId, amount, normalizedKey, attempt, e);
					throw new CustomException(ErrorCode.SPEND_FAIL);
				}

				long delayMs = 100L * (1L << (attempt - 1));
				try {
					Thread.sleep(delayMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					log.error("[POINT] BackOff 대기 중 인터럽트 발생(사용/차감). userId={}, key={}", userId, normalizedKey, ie);
					throw new CustomException(ErrorCode.SPEND_FAIL);
				}
			}
		}

		throw new CustomException(ErrorCode.SPEND_FAIL);
	}

	/**
	 * 포인트 차감 처리 결과입니다.
	 *
	 * @param duplicate      동일 멱등성 키로 이미 처리된 요청인지 여부
	 * @param balanceAfter   차감 처리 후 잔액
	 * @param idempotencyKey 최종 사용된 멱등성 키(요청이 비어 있었던 경우 서버에서 생성)
	 */
	public record SpendResult(boolean duplicate, long balanceAfter, String idempotencyKey) {
		public static SpendResult of(boolean duplicate, long balanceAfter, String idempotencyKey) {
			return new SpendResult(duplicate, balanceAfter, idempotencyKey);
		}
	}

	// ===== private helpers =====

	private void validateChargeUseCase(PointChargeRequest request) {
		if (request == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
	}

	private String normalizeIdempotencyKey(String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return idempotencyKey.trim();
	}

	private void validateUserExists(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}
	}

	private void validateSpendUseCase(Long userId, long amount) {
		if (userId == null || userId <= 0) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
		if (amount <= 0) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
	}
}
