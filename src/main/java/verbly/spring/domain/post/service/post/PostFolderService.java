package verbly.spring.domain.post.service.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.entity.Folder;
import verbly.spring.domain.post.entity.Post;
import verbly.spring.domain.post.exception.PostHandler;
import verbly.spring.domain.post.repository.FolderRepository;
import verbly.spring.domain.post.repository.PostRepository;
import verbly.spring.global.common.code.ErrorStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFolderService {
    private final PostRepository postRepository;
    private final FolderRepository folderRepository;

    @Transactional
    public void movePostToFolder(Long userId, Long postId, Long folderId) {
        Post post = postRepository.findByIdAndAuthorId(postId, userId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));

        Folder folder = folderRepository.findByIdAndOwnerId(folderId, userId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.FOLDER_NOT_FOUND));

        post.moveToFolder(folder);
    }

    @Transactional
    public void removePostFromFolder(Long userId, Long postId) {
        Post post = postRepository.findByIdAndAuthorId(postId, userId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));

        post.moveToFolder(null);
    }
}
