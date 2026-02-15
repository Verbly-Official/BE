package verbly.spring.domain.post.service.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import verbly.spring.domain.notification.enums.NotificationType;
import verbly.spring.domain.notification.service.NotificationService;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostLike;
import verbly.spring.domain.post.entity.PostTag;
import verbly.spring.domain.post.entity.Tag;
import verbly.spring.domain.post.exception.PostHandler;
import verbly.spring.domain.post.repository.PostLikeRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.post.repository.PostTagRepository;
import verbly.spring.domain.post.repository.TagRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.exception.UserHandler;
import verbly.spring.domain.user.repository.UserRepository;
import verbly.spring.global.common.code.ErrorStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostCommandServiceImpl implements PostCommandService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostConverter postConverter;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    private final NotificationService notificationService;

    @Override
    public PostResponseDTO.AddPostLike addPostLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new PostHandler(ErrorStatus.POST_ALREADY_LIKED);
        }
        postLikeRepository.save(new PostLike(user, post));
        postRepository.increaseLikeCount(postId);
        Post updatedPost = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        Boolean isLiked = postLikeRepository.existsByUserAndPost(user, updatedPost);
        try {
            if (!post.getAuthor().getId().equals(user.getId())) {
                notificationService.send(post.getAuthor(), user, NotificationType.LIKE, "/posts/" + postId);
            }
        } catch (Exception e) {
            log.warn("알림 전송 실패 (좋아요는 정상 저장됨): {}", e.getMessage());
        }
        return postConverter.addPostLike(updatedPost, isLiked);
    }

    @Override
    public PostResponseDTO.AddPostLike deletePostLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        PostLike postLike = postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_LIKED));
        postLikeRepository.delete(postLike);
        postRepository.decreaseLikeCount(postId);
        Post updatedPost = postRepository.findById(postId).orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));
        Boolean isLiked = postLikeRepository.existsByUserAndPost(user, updatedPost);
        return postConverter.addPostLike(updatedPost, isLiked);
    }

    @Override
    public PostResponseDTO.HomeWritePost writeHomePost(PostRequestDTO.HomeWritePost dto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        Post newPost = postConverter.toWritePost(dto, user);
        postRepository.save(newPost);
        processTag(newPost, dto.getTags());
        return postConverter.writeHomePost(newPost);
    }

    public void processTag(Post post, List<String> tags){
        if (tags.isEmpty() || tags == null) return;
        Set<String> tagSet = new HashSet<>(tags);

        List<Tag> existingTags = tagRepository.findByNameIn(tags);

        Set<String> existingTagNames = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());

        List<Tag> newTags = tagSet.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> Tag.builder()
                        .count(1)
                        .name(name)
                        .build())
                .toList();
        tagRepository.saveAll(newTags);

        if(!existingTags.isEmpty()){
            List<Long> tagIds = existingTags.stream().map(Tag::getId).collect(Collectors.toList());
            tagRepository.increaseUsageCount(tagIds);
        }

        List<Tag> finalTags = new ArrayList<>();
        finalTags.addAll(existingTags);
        finalTags.addAll(newTags);

        List<PostTag> postTags = finalTags.stream()
                .map(finalTag -> PostTag.builder()
                        .tag(finalTag)
                        .post(post)
                        .build())
                .toList();
        postTagRepository.saveAll(postTags);
    }
}
