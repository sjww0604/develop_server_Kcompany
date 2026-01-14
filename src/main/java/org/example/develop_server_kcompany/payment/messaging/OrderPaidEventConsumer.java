package org.example.develop_server_kcompany.payment.messaging;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.example.develop_server_kcompany.payment.messaging.dto.OrderPaidEventItemPayload;
import org.example.develop_server_kcompany.payment.messaging.dto.OrderPaidEventPayload;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OrderPaidEventConsumer 클래스입니다.
 * <p>
 * Kafka의 "주문 결제 완료" 이벤트를 소비하여, 인기 메뉴 집계를 Redis(ZSET)에 누적합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 14.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPaidEventConsumer {

	private static final String KEY_PREFIX = "popular:menu:";
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
	private static final String DEDUP_PREFIX = "popular:dedup:order:";
	private static final Duration DEDUP_TTL = Duration.ofDays(9);
	private final PopularityRedisRepository popularityRedisRepository;
	private final StringRedisTemplate redisTemplate;
	private final Clock clock;

	/**
	 * Kafka로부터 주문 결제 완료 이벤트를 소비합니다.
	 *
	 * @param payload 주문 결제 완료 이벤트 페이로드
	 */
	@KafkaListener(
		topics = "${app.kafka.topic.payment}",
		groupId = "popular-menu-ranking",
		containerFactory = "orderPaidEventKafkaListenerContainerFactory"
	)
	public void consume(OrderPaidEventPayload payload) {
		if (hasNoItems(payload)) {
			logSkipBecauseEmptyItems(payload);
			return;
		}

		Long orderId = payload.getOrderId();
		if (orderId == null || orderId <= 0) {
			log.warn("[KAFKA] 결제 완료 이벤트 orderId 누락/비정상. payload={}", payload);
			return;
		}

		// Kafka는 at-least-once 전달이 기본이므로(재시도/리밸런스 등), 소비 측에서 중복 처리를 방지합니다.
		// 주문 1건(orderId) 기준으로 한 번만 집계하도록 Redis에 멱등 키를 저장합니다.
		String dedupKey = DEDUP_PREFIX + orderId;
		Boolean first = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", DEDUP_TTL);
		if (Boolean.FALSE.equals(first)) {
			log.info("[KAFKA] 이미 집계된 주문 이벤트입니다. orderId={}", orderId);
			return;
		}

		String yyyyMMdd = LocalDate.now(clock).format(DAY_FORMATTER);
		String redisKey = KEY_PREFIX + yyyyMMdd;

		try {
			for (OrderPaidEventItemPayload item : payload.getItems()) {
				if (isInvalidItem(item)) {
					continue;
				}

				int quantity = normalizeQuantity(item.getQuantity());
				if (quantity == 0) {
					continue;
				}

				// menuId별 판매량(quantity)을 일자별 ZSET에 누적
				popularityRedisRepository.incrementDailyMenuCount(redisKey, item.getMenuId(), quantity);
			}
			log.info("[KAFKA] 인기 메뉴 집계 누적 완료. orderId={}, redisKey={}, itemCount={}",
				orderId, redisKey, payload.getItems().size());
		} catch (RuntimeException e) {

			redisTemplate.delete(dedupKey);
			log.error("[KAFKA] 인기 메뉴 집계 실패. orderId={}, redisKey={}", orderId, redisKey, e);
			throw e;
		}
	}

		private boolean hasNoItems (OrderPaidEventPayload payload){
			return payload == null || payload.getItems() == null || payload.getItems().isEmpty();
		}

		private void logSkipBecauseEmptyItems (OrderPaidEventPayload payload){
			log.warn("[KAFKA] 결제 완료 이벤트 items 비어 있음. orderId={}",
				payload == null ? null : payload.getOrderId());
		}

		private boolean isInvalidItem (OrderPaidEventItemPayload item){
			return item == null || item.getMenuId() == null;
		}

		private int normalizeQuantity ( int quantity){
			return Math.max(quantity, 0);
		}
}
