package verbly.spring.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "folder",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_folder_owner_name", columnNames = {"owner_id", "name"})
        },
        indexes = {
                @Index(name = "idx_folder_owner_created", columnList = "owner_id, created_at")
        }
)
public class Folder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 50)
    private String name;

    public void rename(String name) {
        this.name = name;
    }
}
