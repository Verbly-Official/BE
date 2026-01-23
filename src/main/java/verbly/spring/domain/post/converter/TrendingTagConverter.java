package verbly.spring.domain.post.converter;

import org.springframework.stereotype.Component;
import verbly.spring.domain.post.dto.response.TagResponseDTO;
import verbly.spring.domain.post.entity.TrendingTag;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrendingTagConverter {

    public List<TagResponseDTO.TrendingTags> toTrendingTags(List<TrendingTag> trendingTags) {
        return trendingTags.stream()
                .map(trendingTag -> TagResponseDTO.TrendingTags.builder()
                        .ranking(trendingTag.getRanking())
                        .tagName(trendingTag.getTag().getName())
                        .tagId(trendingTag.getTag().getId())
                        .count(trendingTag.getTag().getCount())
                        .build())
                .collect(Collectors.toList());
    }
}
