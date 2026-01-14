package org.example.develop_server_kcompany.payment.event;

/**
 * OrderCompletedDomainEvent 클래스입니다.
 * <p>
 * 주문이 결제 완료(결제 처리 성공) 상태로 확정되었음을 알리는 도메인 이벤트입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */

public class OrderCompletedDomainEvent {

	private final Long orderId;

	/**
	 * 결제 완료가 확정되었음을 나타내는 도메인 이벤트를 생성합니다.
	 * <p>
	 * 본 이벤트는 결제 트랜잭션 내부에서 발행되며, 실제 Kafka 전송은 AFTER_COMMIT 단계의 이벤트 리스너에서 처리됩니다.
	 * </p>
	 *
	 * @param orderId 주문 식별자
	 */
	public OrderCompletedDomainEvent(Long orderId) {
		this.orderId = orderId;
	}

	public Long getOrderId() {
		return orderId;
	}
}
