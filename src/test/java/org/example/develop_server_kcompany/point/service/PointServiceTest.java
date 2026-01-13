package org.example.develop_server_kcompany.point.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.example.develop_server_kcompany.point.domain.PointTransaction;
import org.example.develop_server_kcompany.point.domain.PointWallet;
import org.example.develop_server_kcompany.point.dto.PointChargeRequest;
import org.example.develop_server_kcompany.point.dto.PointChargeResponse;
import org.example.develop_server_kcompany.point.repository.PointTransactionRepository;
import org.example.develop_server_kcompany.point.repository.PointWalletRepository;
import org.example.develop_server_kcompany.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * PointService 단위 테스트 클래스입니다.
 * <p>
 * Spring Context를 로드하지 않고, Repository 및 TransactionTemplate을 Mock으로 대체하여
 * 포인트 충전 유스케이스의 비즈니스 로직만 검증합니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 13.
 */
@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PointWalletRepository pointWalletRepository;

	@Mock
	private PointTransactionRepository pointTransactionRepository;

	@Mock
	private TransactionTemplate transactionTemplate;

	@InjectMocks
	private PointService pointService;

	@Test
	@DisplayName("포인트 충전 성공 - 등록된 사용자 지갑 충전")
	void charge_success_registeredUser_walletExists() {
		// given
		Long userId = 1L;
		Long amount = 1000L;

		PointChargeRequest request = mock(PointChargeRequest.class);
		when(request.getUserId()).thenReturn(userId);
		when(request.getAmount()).thenReturn(amount);
		when(request.getIdempotencyKey()).thenReturn("test-key");

		when(userRepository.existsById(userId)).thenReturn(true);

		when(transactionTemplate.execute(any(TransactionCallback.class)))
			.thenAnswer(invocation -> {
				TransactionCallback<?> callback = invocation.getArgument(0);
				return callback.doInTransaction(null);
			});

		when(pointTransactionRepository.findByUserIdAndIdempotencyKey(userId, "test-key"))
			.thenReturn(Optional.empty());

		PointWallet wallet = new PointWallet(userId);
		when(pointWalletRepository.findById(userId))
			.thenReturn(Optional.of(wallet));

		when(pointTransactionRepository.save(any(PointTransaction.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		//when
		PointChargeResponse response = pointService.charge(request);

		// then
		assertThat(response.isDuplicate()).isFalse();
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getChargedAmount()).isEqualTo(amount);
		assertThat(response.getBalanceAfter()).isEqualTo(amount);

		verify(pointTransactionRepository).findByUserIdAndIdempotencyKey(userId, "test-key");
		verify(userRepository).existsById(userId);
		verify(pointWalletRepository, never()).save(any(PointWallet.class));
		verify(pointTransactionRepository).save(any(PointTransaction.class));
	}
}
