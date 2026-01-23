package verbly.spring.domain.post.service.trending;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import verbly.spring.domain.post.converter.TrendingTagConverter;
import verbly.spring.domain.post.dto.response.TagResponseDTO;
import verbly.spring.domain.post.entity.TrendingTag;
import verbly.spring.domain.post.repository.TrendingTagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendingTagQueryServiceImpl implements TrendingTagQueryService {
    private final TrendingTagRepository trendingTagRepository;
    private final TrendingTagConverter trendingTagConverter;

    @Override
    public List<TagResponseDTO.TrendingTags> getTrendingTags() {
        List<TrendingTag> trendingTags = trendingTagRepository.findAll();
        return trendingTagConverter.toTrendingTags(trendingTags);
    }
}
