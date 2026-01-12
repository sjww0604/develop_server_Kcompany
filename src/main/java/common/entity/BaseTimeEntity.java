package common.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * BaseTimeEntity 인터페이스입니다.
 * <p>
 * 생성일시, 수정일시를 관리하기 위한 추상 클래스입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 12.
 */
@Getter
@MappedSuperclass // BaseEntity를 상속한 엔티티들은 아래 필드들을 컬럼으로 인식하게 된다.
@EntityListeners(AuditingEntityListener.class)  // Auditing(자동으로 값 매핑) 기능 추가
public abstract class BaseTimeEntity {

	@CreatedDate
	private LocalDateTime createdTime;

	@LastModifiedDate
	private LocalDateTime lastModifiedTime;
}