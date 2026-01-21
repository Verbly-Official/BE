package verbly.spring.domain.correction.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "correction")
public class Correction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
