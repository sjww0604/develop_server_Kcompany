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
 * 주문 목록 엔티티입니다.
 * <p>
 * 주문 항목은 하나의 주문({@link Order})에 소속되며,
 * 특정 메뉴에 대한 주문 수량과 금액 정보를 관리합니다.
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

	protected OrderItem(Long menuId, String menuNameSnapshot, Long unitPriceSnapshot, int quantity) {
		this.menuId = menuId;
		this.menuNameSnapshot = menuNameSnapshot;
		this.unitPriceSnapshot = unitPriceSnapshot;
		this.quantity = quantity;
		this.lineAmount = Math.multiplyExact(unitPriceSnapshot, (long) quantity);
	}

	void attach(Order order) {
		this.order = order;
	}

	public static OrderItem create(
		Long menuId,
		String menuNameSnapshot,
		Long unitPriceSnapshot,
		int quantity
	) {
		return new OrderItem(menuId, menuNameSnapshot, unitPriceSnapshot, quantity);
	}
}
