package verbly.spring.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.dto.response.TagResponseDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendingTagQueryServiceImpl implements TrendingTagQueryService {
    @Override
    public List<TagResponseDTO.TrendingTags> getTrendingTags() {
        return List.of();
    }
}
