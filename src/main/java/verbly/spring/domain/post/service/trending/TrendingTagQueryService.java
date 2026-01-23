package verbly.spring.domain.post.service.trending;

import verbly.spring.domain.post.dto.response.TagResponseDTO;

import java.util.List;

public interface TrendingTagQueryService {
    List<TagResponseDTO.TrendingTags> getTrendingTags();
}
