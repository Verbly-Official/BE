package verbly.spring.domain.post.service.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import verbly.spring.domain.post.converter.PostConverter;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.entity.PostLike;
import verbly.spring.domain.post.entity.PostTag;
import verbly.spring.domain.post.entity.Tag;
import verbly.spring.domain.post.repository.PostLikeRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.domain.post.repository.PostTagRepository;
import verbly.spring.domain.post.repository.TagRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandServiceImpl implements PostCommandService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostConverter postConverter;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Override
    public PostResponseDTO.AddPostLike addPostLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));
        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }
        postLikeRepository.save(new PostLike(user, post));
        postRepository.increaseLikeCount(postId);
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        Boolean isLiked = postLikeRepository.existsByUserAndPost(user, updatedPost);
        return postConverter.addPostLike(updatedPost, isLiked);
    }

    @Override
    public PostResponseDTO.AddPostLike deletePostLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));
        PostLike postLike = postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalStateException("이미 좋아요를 취소했거나, 누른 적이 없습니다."));
        postLikeRepository.delete(postLike);
        postRepository.decreaseLikeCount(postId);
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        Boolean isLiked = postLikeRepository.existsByUserAndPost(user, updatedPost);
        return postConverter.addPostLike(updatedPost, isLiked);
    }

    @Override
    public PostResponseDTO.HomeWritePost writeHomePost(PostRequestDTO.HomeWritePost dto, User user) {
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
