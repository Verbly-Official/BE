package verbly.spring.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import verbly.spring.domain.post.entity.Comment;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostLike;
import verbly.spring.domain.stats.entity.Stats;
import verbly.spring.domain.user.enums.AuthProvider;
import verbly.spring.domain.user.enums.UserStatus;
import verbly.spring.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Column(length = 3)
    private String nativeLang; // ISO code (e.g. "kr")

    @Column(length = 3)
    private String learningLang; // ISO code (e.g. "en")

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // 연관된 자식 엔티티가 부모에서 제거되었을 때, DB에서도 자동 삭제되도록
    private ProfileImage profileImage;

    @Column(length = 50)
    private String nickname;

    @Column(columnDefinition = "TEXT") // 길이 제한 없도록
    private String bio;

    @Column(length = 50)
    private String email;

    @Column(length = 20)
    private String phoneNumber; // +82 010-1234-5678

    @Column(length = 50)
    private String timezone;

    @Column(columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID uuid;

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // ONBOARDING(소셜 로그인 직후), ACTIVE(온보딩 완료), SUSPENDED, DELETED

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // 연관된 자식 엔티티가 부모에서 제거되었을 때, DB에서도 자동 삭제되도록
    private NotificationSettings notificationSettings;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stats stats;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void setStats(Stats stats) {
        this.stats = stats;
        if (stats.getUser() != this) {
            stats.setUser(this); // 양방향 연관관계 세팅
        }
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateBio(String bio) {
        this.bio = bio;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
