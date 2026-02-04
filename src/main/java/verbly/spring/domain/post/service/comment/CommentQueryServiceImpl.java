package verbly.spring.domain.post.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.converter.CommentConverter;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.entity.Comment;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.exception.PostHandler;
import verbly.spring.domain.post.repository.CommentRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.global.common.code.ErrorStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentConverter commentConverter;

    @Override
    public Slice<CommentResponseDTO.getComment> getComments(Pageable pageable, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        Slice<Comment> comments = commentRepository.findByPost(pageable, post);
        return comments.map( comment -> commentConverter.toGetCommnet(comment));
    }
}
