package verbly.spring.domain.correction.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import verbly.spring.domain.correction.dto.response.CorrectionEditorQueryDTO;
import verbly.spring.domain.correction.entity.QCorrection;
import verbly.spring.domain.correction.entity.QCorrectionFeedback;
import verbly.spring.domain.correction.entity.QCorrectionWord;
import verbly.spring.domain.post.entity.QPost;
import verbly.spring.domain.user.entity.QUser;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CorrectionEditorQueryRepositoryImpl implements CorrectionEditorQueryRepository{
    private final JPAQueryFactory queryFactory;

    private static final QCorrection correction = QCorrection.correction;
    private static final QPost post = QPost.post;
    private static final QCorrectionWord word = QCorrectionWord.correctionWord;
    private static final QCorrectionFeedback feedback = QCorrectionFeedback.correctionFeedback;
    private static final QUser user = QUser.user;

    @Override
    public Optional<CorrectionEditorQueryDTO.CorrectionBaseRow> findCorrectionBase(Long correctionId) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(
                                CorrectionEditorQueryDTO.CorrectionBaseRow.class,
                                correction.id,
                                post.id,
                                post.status,
                                post.content
                        ))
                        .from(correction)
                        .join(correction.post, post)
                        .where(correction.id.eq(correctionId))
                        .fetchOne()
        );
    }

    @Override
    public List<CorrectionEditorQueryDTO.WordRow> findWords(Long correctionId) {
        return queryFactory
                .select(Projections.constructor(
                        CorrectionEditorQueryDTO.WordRow.class,
                        word.id,
                        word.sentenceIdx,
                        word.startIdx,
                        word.endIdx,
                        word.originalText,
                        word.correctedText
                ))
                .from(word)
                .where(word.correction.id.eq(correctionId))
                .orderBy(word.sentenceIdx.asc(), word.startIdx.asc())
                .fetch();
    }

    @Override
    public List<CorrectionEditorQueryDTO.FeedbackRow> findFeedback(Long correctionId) {
        return queryFactory
                .select(Projections.constructor(
                        CorrectionEditorQueryDTO.FeedbackRow.class,
                        feedback.id,
                        feedback.sentenceIdx,
                        user.nickname,
                        feedback.correctorType,
                        feedback.content,
                        feedback.createdAt,
                        feedback.updatedAt
                ))
                .from(feedback)
                .join(feedback.corrector, user)
                .where(feedback.correction.id.eq(correctionId))
                .orderBy(feedback.createdAt.desc())
                .fetch();
    }
}
