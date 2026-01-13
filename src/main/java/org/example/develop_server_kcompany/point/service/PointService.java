package org.example.develop_server_kcompany.point.service;

import org.example.develop_server_kcompany.common.exception.CustomException;
import org.example.develop_server_kcompany.common.exception.ErrorCode;
import org.example.develop_server_kcompany.point.domain.PointTransaction;
import org.example.develop_server_kcompany.point.domain.PointWallet;
import org.example.develop_server_kcompany.point.dto.PointChargeRequest;
import org.example.develop_server_kcompany.point.dto.PointChargeResponse;
import org.example.develop_server_kcompany.point.enums.PointTransactionType;
import org.example.develop_server_kcompany.point.repository.PointTransactionRepository;
import org.example.develop_server_kcompany.point.repository.PointWalletRepository;
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

	private final PointWalletRepository pointWalletRepository;
	private final PointTransactionRepository pointTransactionRepository;
	private final TransactionTemplate transactionTemplate;

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
						PointWallet pointWallet = pointWalletRepository.findById(userId)
							.orElseGet(() -> pointWalletRepository.save(new PointWallet(userId)));

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

						log.info("[POINT] 포인트 충전 성공. userId={}, amount={}, balanceAfter={}, key={}", userId, amount,
							balanceAfter, idempotencyKey);
						return PointChargeResponse.of(false, userId, amount, balanceAfter, idempotencyKey);
					}));
			} catch (ObjectOptimisticLockingFailureException e) {
				log.warn("[POINT] 낙관적 락 충돌 재시도. attempt={}, userId={}, key={}", attempt, userId, idempotencyKey);
				if (attempt == 3) {
					throw e;
				}

				long delayMs = 100L * (1L << (attempt - 1));
				try {
					Thread.sleep(delayMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("BackOff 대기 중 인터럽트 발생", ie);
				}
			}
		}
		throw new CustomException(ErrorCode.CHARGE_FAIL);
	}

	private void validateChargeUseCase(PointChargeRequest request) {
		if (request == null) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
	}

	private String normalizeIdempotencyKey(String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return java.util.UUID.randomUUID().toString();
		}
		return idempotencyKey.trim();
	}
}
