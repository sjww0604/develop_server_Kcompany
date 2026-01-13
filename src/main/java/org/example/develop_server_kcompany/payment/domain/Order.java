package org.example.develop_server_kcompany.payment.domain;

import java.util.ArrayList;
import java.util.List;

import org.example.develop_server_kcompany.common.entity.BaseTimeEntity;
import org.example.develop_server_kcompany.payment.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 엔티티 클래스입니다.
 * <p>
 * 하나의 주문은 특정 사용자(userId)에 의해 생성되며,
 * 여러 개의 주문 항목({@link OrderItem})을 가질 수 있습니다.
 *  주문은 생성 시점에 {@link OrderStatus#CREATED} 상태로 시작하며,
 *  결제 완료 시 {@link OrderStatus#PAID} 상태로 변경됩니다.
 *  주문에 포함된 항목들의 금액 합계를 {@code totalAmount}로 관리합니다.
 * </p>
 *
 * <p>
 * 멱등성 키(idempotencyKey)는 동일 사용자의 중복 주문 생성을 방지하기 위해 사용되며,
 * {@code (userId + idempotencyKey)} 조합은 DB 레벨에서 유니크 제약조건으로 보호됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Entity
@Table(name = "orders",
	indexes = {
		@Index(name = "idx_orders_user", columnList = "user_id")
	}
	, uniqueConstraints = {
	@UniqueConstraint(name = "uk_orders_user_idem", columnNames = {"user_id", "idempotency_key"})
}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "total_amount", nullable = false)
	private Long totalAmount;

	@Column(name = "idempotency_key", nullable = false, length = 100)
	private String idempotencyKey;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	/**
	 * 주문을 생성합니다.
	 *
	 * <p>
	 * 주문은 생성 시 {@link OrderStatus#CREATED} 상태로 초기화되며,
	 * 총 금액은 0으로 설정됩니다.
	 * </p>
	 *
	 * @param userId 주문을 생성한 사용자 식별자
	 * @param idempotencyKey 중복 주문 방지를 위한 멱등성 키
	 */
	public Order(Long userId, String idempotencyKey) {
		this.userId = userId;
		this.idempotencyKey = idempotencyKey;
		this.status = OrderStatus.CREATED;
		this.totalAmount = 0L;
	}

	/**
	 * 주문에 주문 항목을 추가합니다.
	 *
	 * <p>
	 * 주문 항목을 추가하면서 양방향 연관관계를 설정하고,
	 * 항목의 라인 금액을 주문의 총 금액에 합산합니다.
	 * </p>
	 *
	 * @param item 추가할 주문 항목
	 */
	public void addItem(OrderItem item) {
		this.items.add(item);
		item.attach(this);
		this.totalAmount = Math.addExact(this.totalAmount, item.getLineAmount());
	}

	/**
	 * 주문 상태를 결제 완료(PAID)로 변경합니다.
	 *
	 * <p>
	 * 실제 결제 승인 이후 호출되는 메서드로,
	 * 주문의 상태 전이를 명시적으로 표현하기 위해 제공됩니다.
	 * </p>
	 */
	public void markPaid() {
		this.status = OrderStatus.PAID;
	}
}

