package verbly.spring.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.global.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProfileImage extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String imageUrl;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User user;

        public void updateImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
}
