package verbly.spring.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@DynamicInsert // insert 시 null인 필드를 제외하고 실제 값이 있는 컬럼만 포함하여 INSERT 문을 생성
@DynamicUpdate // update 시 변경된 컬럼만 포함해서 SQL UPDATE 문을 동적으로 생성
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Stats {
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // total_posts(전체 글 개수), correction_received(도움받은 글 개수), correction_given(도움 준 글 개수)는 단순 숫자만 사용하게 될 예정
    // -> 조회 시에만 서비스에서 계산되는 방식
    // total_posts -> post 테이블의 로그인한 user_id 수
    // correction_received -> feedback 테이블의 user_id 수
    // correction_given -> correction테이블의 user_id 수

    // 아래는 행동 시 계산
    private long point; // 레벨 시스템 기준 (7레벨까지)
    /*
    LV 1 (뉴비): 0 ~ 100 P (가입 즉시)
    LV 2 (입문자): 101 ~ 300 P
    LV 3 (숙련자): 301 ~ 600 P
    LV 4 (전문가): 601 ~ 1,000 P
    LV 5 (마스터): 1,001 ~ 1,500 P
    LV 6 (그랜드 마스터): 1,501 ~ 2,100 P
    LV 7 (레전드): 2,101 P
    */

    private int streakDays; // 배지 획득 기준(7일 연속 출석 시, 성실한 출석왕 뱃지 활성화)

    private LocalDate lastActiveDate;

    public void markAttendance(String timezone) {
        LocalDate today = LocalDate.now(ZoneId.of(timezone));

        if (lastActiveDate == null) {
            streakDays = 1;
        } else if (lastActiveDate.equals(today)) {
            return;
        } else if (lastActiveDate.plusDays(1).equals(today)) {
            streakDays++;
        } else {
            streakDays = 1;
        }

        lastActiveDate = today;
    }

    @Column(name = "review_count")
    private Long reviewCount;

    @Column(name = "review_average")
    private Double reviewAverage;

    public void updateReviewMeta(Long reviewCount, Double averageByRevieweeId) {
        this.reviewCount = reviewCount + 1;
        this.reviewAverage = averageByRevieweeId;
    }
}
