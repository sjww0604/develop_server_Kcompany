package org.example.develop_server_kcompany.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 항목 엔티티입니다.
 * <p>
 * 주문 항목은 하나의 주문({@link Order})에 소속되며,
 * 특정 메뉴에 대한 주문 수량과 금액 정보를 관리합니다.
 * 주문 시점의 메뉴 이름과 가격을 스냅샷 형태로 저장하여,
 * 이후 메뉴 정보가 변경되더라도 주문 데이터의 정합성을 유지합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Entity
@Table(name = "order_items",
	indexes = {
	@Index(name = "idx_order_items_order", columnList = "order_id")
	})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "menu_id", nullable = false)
	private Long menuId;

	@Column(name = "menu_name_snapshot", nullable = false, length = 500)
	private String menuNameSnapshot;

	@Column(name = "unit_price_snapshot", nullable = false)
	private Long unitPriceSnapshot;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "line_amount", nullable = false)
	private Long lineAmount;

	/**
	 * 주문 항목을 생성하는 정적 팩토리 메서드입니다.
	 *
	 * <p>
	 * 메뉴 단가와 수량을 기반으로 라인 금액(lineAmount)을 계산하여 저장합니다.
	 * </p>
	 *
	 * @param menuId 주문한 메뉴 식별자
	 * @param menuNameSnapshot 주문 시점의 메뉴 이름
	 * @param unitPriceSnapshot 주문 시점의 메뉴 단가
	 * @param quantity 주문 수량
	 * @return 생성된 주문 항목 엔티티
	 */
	public static OrderItem create(Long menuId, String menuNameSnapshot, Long unitPriceSnapshot, int quantity) {
		return new OrderItem(menuId, menuNameSnapshot, unitPriceSnapshot, quantity);
	}

	/**
	 * 주문 항목을 생성합니다.
	 *
	 * <p>
	 * 생성 규칙을 강제하기 위해 외부에서는 {@link #create(Long, String, Long, int)}를 사용합니다.
	 * </p>
	 */
	private OrderItem(Long menuId, String menuNameSnapshot, Long unitPriceSnapshot, int quantity) {
		this.menuId = menuId;
		this.menuNameSnapshot = menuNameSnapshot;
		this.unitPriceSnapshot = unitPriceSnapshot;
		this.quantity = quantity;
		this.lineAmount = Math.multiplyExact(unitPriceSnapshot, (long) quantity);
	}

	/**
	 * 주문 항목을 특정 주문에 연결합니다.
	 *
	 * <p>
	 * 양방향 연관관계 설정을 위해 사용되며,
	 * {@link Order} 엔티티에서만 호출되도록 접근 범위를 제한합니다.
	 * </p>
	 *
	 * @param order 연결할 주문
	 */
	void attach(Order order) {
		this.order = order;
	}
}
