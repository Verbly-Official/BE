package verbly.spring.domain.correction.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.enums.CorrectorType;
import verbly.spring.domain.post.enums.PostStatus;

import java.util.ArrayList;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.FALSE;
import static com.querydsl.core.types.dsl.Expressions.nullExpression;
import static verbly.spring.domain.correction.entity.QCorrection.correction;
import static verbly.spring.domain.correction.entity.QCorrectionBookmark.correctionBookmark;
import static verbly.spring.domain.correction.entity.QCorrectionFeedback.correctionFeedback;
import static verbly.spring.domain.post.entity.QPost.post;
import static verbly.spring.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class CorrectionQueryRepositoryImpl implements CorrectionQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CorrectionResponseDTO.MyCorrectionListDto> findMyCorrections(
            Long authorId,
            Boolean bookmark,
            Boolean sort,
            PostStatus status,
            CorrectorType correctorType
    ) {
        // TEMP일 경우
        if (status == PostStatus.TEMP) {
            return queryFactory
                    .select(Projections.fields(
                            CorrectionResponseDTO.MyCorrectionListDto.class,
                            ExpressionUtils.as(nullExpression(Long.class), "correctionId"),
                            post.id.as("postId"),
                            post.title.as("title"),
                            post.status.as("status"),
                            ExpressionUtils.as(FALSE, "bookmark"),
                            ExpressionUtils.as(nullExpression(CorrectorType.class), "correctorType"),
                            ExpressionUtils.as(nullExpression(String.class), "correctorName"),
                            post.createdAt.as("correctionCreatedAt"),
                            post.updatedAt.as("correctionUpdatedAt")
                    ))
                    .from(post)
                    .where(
                            post.author.id.eq(authorId),
                            post.status.eq(PostStatus.TEMP)
                    )
                    .orderBy(Boolean.TRUE.equals(sort) ? post.createdAt.desc() : post.id.desc())
                    .fetch();
        }

        BooleanBuilder where = new BooleanBuilder();
        where.and(post.author.id.eq(authorId));

        if (status != null) where.and(post.status.eq(status));

        var latestCreatedAtSubQuery =
                JPAExpressions.select(correctionFeedback.createdAt.max())
                        .from(correctionFeedback)
                        .where(correctionFeedback.correction.eq(correction));

        var latestFeedback = new verbly.spring.domain.correction.entity.QCorrectionFeedback("latestFeedback");

        var correctorNameExpr = new CaseBuilder()
                .when(latestFeedback.correctorType.eq(CorrectorType.AI_ASSISTANT))
                .then("AI Assistant")
                .when(latestFeedback.correctorType.eq(CorrectorType.NATIVE_SPEAKER))
                .then(user.nickname)
                .otherwise((String) null);

        var bookmarkedExpr = correctionBookmark.id.isNotNull();

        if (bookmark != null) {
            where.and(Boolean.TRUE.equals(bookmark) ? bookmarkedExpr : bookmarkedExpr.not());
        }

        var baseQuery = queryFactory
                .select(Projections.fields(
                        CorrectionResponseDTO.MyCorrectionListDto.class,
                        correction.id.as("correctionId"),
                        post.id.as("postId"),
                        post.title.as("title"),
                        post.status.as("status"),
                        bookmarkedExpr.as("bookmark"),
                        latestFeedback.correctorType.as("correctorType"),
                        correctorNameExpr.as("correctorName"),
                        correction.createdAt.as("correctionCreatedAt"),
                        correction.updatedAt.as("correctionUpdatedAt")
                ))
                .from(correction)
                .join(correction.post, post)
                .leftJoin(correctionBookmark).on(
                        correctionBookmark.correction.eq(correction)
                                .and(correctionBookmark.user.id.eq(authorId))
                )
                .leftJoin(latestFeedback).on(
                        latestFeedback.correction.eq(correction)
                                .and(latestFeedback.createdAt.eq(latestCreatedAtSubQuery))
                )
                .leftJoin(latestFeedback.corrector, user)
                .where(where);

        if (correctorType != null) {
            baseQuery.where(latestFeedback.correctorType.eq(correctorType));
        }

        baseQuery.orderBy(Boolean.TRUE.equals(sort) ? correction.createdAt.desc() : correction.id.desc());

        return baseQuery.fetch();
    }

    @Override
    public Page<CorrectionResponseDTO.MyCorrectionDto> findNativeCorrectionRequests(
            Long userId,
            Boolean bookmark,
            PostStatus status,
            Pageable pageable
    ) {
        var author = new verbly.spring.domain.user.entity.QUser("author");
        var correctorUser = new verbly.spring.domain.user.entity.QUser("correctorUser");

        var bookmarkedExpr = correctionBookmark.id.isNotNull();

        BooleanBuilder where = new BooleanBuilder();
        where.and(author.learningLang.eq("en"));
        where.and(post.status.ne(PostStatus.TEMP));
        where.and(statusEq(status));

        where.and(
                post.status.ne(PostStatus.PENDING)
                        .or(correction.correctorType.eq(CorrectorType.AI_ASSISTANT))
        );

        if (status == PostStatus.PENDING) {
            where.and(correction.correctorType.eq(CorrectorType.AI_ASSISTANT));
        }

        if (bookmark != null) {
            where.and(Boolean.TRUE.equals(bookmark) ? bookmarkedExpr : bookmarkedExpr.not());
        }

        List<OrderSpecifier<?>> orderSpecifiers = resolveSort(pageable.getSort());

        var correctorNameExpr = new CaseBuilder()
                .when(correction.correctorType.eq(CorrectorType.AI_ASSISTANT))
                .then("AI Assistant")
                .when(correction.correctorType.eq(CorrectorType.NATIVE_SPEAKER))
                .then(correctorUser.nickname)
                .otherwise((String) null);

        List<CorrectionResponseDTO.MyCorrectionDto> content = queryFactory
                .select(Projections.fields(
                        CorrectionResponseDTO.MyCorrectionDto.class,
                        correction.id.as("correctionId"),
                        post.id.as("postId"),
                        post.title.as("title"),
                        post.status.as("status"),
                        post.content.as("content"),
                        bookmarkedExpr.as("bookmark"),
                        correction.correctorType.as("correctorType"),
                        correctorNameExpr.as("correctorName"),
                        correction.createdAt.as("correctionCreatedAt"),
                        correction.updatedAt.as("correctionUpdatedAt")
                ))
                .from(correction)
                .join(correction.post, post)
                .join(post.author, author)
                .leftJoin(correction.corrector, correctorUser)
                .leftJoin(correctionBookmark).on(
                        correctionBookmark.correction.eq(correction)
                                .and(correctionBookmark.user.id.eq(userId))
                )
                .where(where)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(correction.id.countDistinct())
                .from(correction)
                .join(correction.post, post)
                .join(post.author, author)
                .leftJoin(correctionBookmark).on(
                        correctionBookmark.correction.eq(correction)
                                .and(correctionBookmark.user.id.eq(userId))
                )
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public long countMyCorrections(
            Long userId,
            Boolean bookmark,
            PostStatus status,
            CorrectorType correctorType
    ) {
        // TEMP는 집계 제외
        if (status == PostStatus.TEMP) {
            return 0L;
        }

        BooleanBuilder where = new BooleanBuilder();
        where.and(post.author.id.eq(userId));

        if (status != null) {
            where.and(post.status.eq(status));
        }

        var latestCreatedAtSubQuery =
                JPAExpressions.select(correctionFeedback.createdAt.max())
                        .from(correctionFeedback)
                        .where(correctionFeedback.correction.eq(correction));

        var latestFeedback =
                new verbly.spring.domain.correction.entity.QCorrectionFeedback("latestFeedback");

        var bookmarkedExpr = correctionBookmark.id.isNotNull();
        if (bookmark != null) {
            where.and(Boolean.TRUE.equals(bookmark) ? bookmarkedExpr : bookmarkedExpr.not());
        }

        // count 쿼리
        var countQuery = queryFactory
                .select(correction.id.countDistinct())
                .from(correction)
                .join(correction.post, post)
                .leftJoin(correctionBookmark).on(
                        correctionBookmark.correction.eq(correction)
                                .and(correctionBookmark.user.id.eq(userId))
                )
                .leftJoin(latestFeedback).on(
                        latestFeedback.correction.eq(correction)
                                .and(latestFeedback.createdAt.eq(latestCreatedAtSubQuery))
                )
                .where(where);

        if (correctorType != null) {
            countQuery.where(latestFeedback.correctorType.eq(correctorType));
        }

        Long total = countQuery.fetchOne();
        return total == null ? 0L : total;
    }

    @Override
    public long countNativeCorrectionRequests(
            Long userId,
            Boolean bookmark,
            PostStatus status
    ) {
        var author = new verbly.spring.domain.user.entity.QUser("author");
        var bookmarkedExpr = correctionBookmark.id.isNotNull();

        BooleanBuilder where = new BooleanBuilder();
        where.and(author.learningLang.eq("en"));
        where.and(post.status.ne(PostStatus.TEMP));
        where.and(statusEq(status));

        where.and(
                post.status.ne(PostStatus.PENDING)
                        .or(correction.correctorType.eq(CorrectorType.AI_ASSISTANT))
        );

        if (status == PostStatus.PENDING) {
            where.and(correction.correctorType.eq(CorrectorType.AI_ASSISTANT));
        }

        if (bookmark != null) {
            where.and(Boolean.TRUE.equals(bookmark) ? bookmarkedExpr : bookmarkedExpr.not());
        }

        Long total = queryFactory
                .select(correction.id.countDistinct())
                .from(correction)
                .join(correction.post, post)
                .join(post.author, author)
                .leftJoin(correctionBookmark).on(
                        correctionBookmark.correction.eq(correction)
                                .and(correctionBookmark.user.id.eq(userId))
                )
                .where(where)
                .fetchOne();

        return total == null ? 0L : total;
    }



    private List<OrderSpecifier<?>> resolveSort(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort != null && sort.isSorted()) {
            for (Sort.Order o : sort) {
                String p = o.getProperty();
                boolean asc = o.isAscending();

                switch (p) {
                    case "id" -> orders.add(asc ? correction.id.asc() : correction.id.desc());
                    case "createdAt" -> orders.add(asc ? correction.createdAt.asc() : correction.createdAt.desc());
                    case "updatedAt" -> orders.add(asc ? correction.updatedAt.asc() : correction.updatedAt.desc());
                    default -> {
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            orders.add(correction.id.desc());
        }
        return orders;
    }

    private BooleanExpression statusEq(PostStatus status) {
        return status == null ? null : post.status.eq(status);
    }

}
