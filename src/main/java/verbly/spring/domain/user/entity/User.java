package verbly.spring.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import verbly.spring.domain.user.enums.AuthProvider;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // user는 예약어라 users로
@Getter
@Setter
@DynamicInsert // insert 시 null인 필드를 제외하고 실제 값이 있는 컬럼만 포함하여 INSERT 문을 생성
@DynamicUpdate // update 시 변경된 컬럼만 포함해서 SQL UPDATE 문을 동적으로 생성
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // 자바 enum 값을 문자열(String) 형태로 DB에 저장
    @Column(nullable = false) // null 불가 컬럼
    private AuthProvider provider; // KAKAO, GOOGLE

    @Column(nullable = false, unique = true)
    private String socialId;

    @Column(nullable = false, length = 10)
    private String nativeLang; // ISO code (e.g. "ko")

    @Column(nullable = false, length = 10)
    private String learningLang; // ISO code (e.g. "en")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(columnDefinition = "TEXT") // 길이 제한 없도록
    private String bio;

    //    private String profileImage;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // 연관된 자식 엔티티가 부모에서 제거되었을 때, DB에서도 자동 삭제되도록
    private ProfileImage profileImage;

    @Column(nullable = false, length = 50)
    private String timezone;

    @Column(length = 50)
    private String email;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
