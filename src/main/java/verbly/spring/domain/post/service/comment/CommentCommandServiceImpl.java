package verbly.spring.domain.post.service.comment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import verbly.spring.domain.notification.enums.NotificationType;
import verbly.spring.domain.notification.service.NotificationService;
import verbly.spring.domain.post.converter.CommentConverter;
import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.entity.Comment;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.exception.PostHandler;
import verbly.spring.domain.post.repository.CommentRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentCommandServiceImpl implements CommentCommandService{

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final NotificationService notificationService;
    private final CommentConverter commentConverter;

    @Override
    public CommentResponseDTO.getMyComment getMyComment(Long userId, Long postId, CommentRequestDTO.makeComment dto) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        Comment comment = commentConverter.toComment(dto, user, post);
        commentRepository.save(comment);
        postRepository.increaseCommentCount(postId);
        try {
            if (!post.getAuthor().getId().equals(user.getId())) {
                notificationService.send(post.getAuthor(), user, NotificationType.COMMENT, "/posts/" + postId);
            }
        } catch (Exception e) {
            log.warn("알림 전송 실패 (좋아요는 정상 저장됨): {}", e.getMessage());
        }
        Post updatedPost = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        return commentConverter.toCommnetDTO(comment, updatedPost);
    }
}
