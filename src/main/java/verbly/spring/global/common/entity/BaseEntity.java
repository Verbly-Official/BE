package verbly.spring.global.common.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass // 이 클래스를 상속하는 엔티티들이 이 클래스에 선언된 필드들을 자기 테이블 컬럼으로 상속받게 함
@EntityListeners(AuditingEntityListener.class) // AuditingEntityListener는 생성시간(@CreatedDate), 수정시간(@LastModifiedDate) 자동 업데이트를 지원
@Getter
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
