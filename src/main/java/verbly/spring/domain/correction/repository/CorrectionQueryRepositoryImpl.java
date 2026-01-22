package verbly.spring.domain.correction.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.enums.PostStatus;

import java.util.List;

import static verbly.spring.domain.correction.entity.QCorrection.correction;
import static verbly.spring.domain.correction.entity.QCorrectionFeedback.correctionFeedback;
import static verbly.spring.domain.post.entity.QPost.post;
import static verbly.spring.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class CorrectionQueryRepositoryImpl implements CorrectionQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CorrectionResponseDTO.MyCorrectionDto> findMyCorrections(
            Long authorId,
            Boolean bookmark,
            Boolean sort,
            PostStatus status,
            CorrectorType correctorType
    ) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(post.author.id.eq(authorId));

//        if (Boolean.TRUE.equals(bookmark)) {
//            where.and(post.bookmark.isTrue());
//        }

        if (status != null) {
            where.and(post.status.eq(status));
        }

        var latestCreatedAtSubQuery =
                JPAExpressions.select(correctionFeedback.createdAt.max())
                        .from(correctionFeedback)
                        .where(correctionFeedback.correction.eq(correction));


        var latestFeedback = new verbly.spring.domain.correction.entity.QCorrectionFeedback("latestFeedback");

        var baseQuery = queryFactory
                .select(Projections.constructor(
                        CorrectionResponseDTO.MyCorrectionDto.class,
                        correction.id,
                        post.id,
                        post.title,
                        post.status,
                        latestFeedback.correctorType,
                        new CaseBuilder()
                                .when(latestFeedback.correctorType.eq(CorrectorType.AI_ASSISTANT))
                                .then("AI Assistant")
                                .when(latestFeedback.correctorType.eq(CorrectorType.NATIVE_SPEAKER))
                                .then(user.nickname)
                                .otherwise((String) null),
                        correction.createdAt       // correctionCreatedAt
                ))
                .from(correction)
                .join(correction.post, post)
                .leftJoin(latestFeedback)
                .on(
                        latestFeedback.correction.eq(correction)
                                .and(latestFeedback.createdAt.eq(latestCreatedAtSubQuery))
                )
                .leftJoin(latestFeedback.corrector, user)
                .where(where);

        if (correctorType != null) {
            baseQuery.where(latestFeedback.correctorType.eq(correctorType));
        }

        if (Boolean.TRUE.equals(sort)) {
            baseQuery.orderBy(correction.createdAt.desc());
        } else {
            baseQuery.orderBy(correction.id.desc());
        }

        return baseQuery.fetch();
    }

}
