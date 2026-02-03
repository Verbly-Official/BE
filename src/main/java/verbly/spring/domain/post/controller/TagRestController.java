package verbly.spring.domain.post.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.post.dto.response.TagResponseDTO;
import verbly.spring.domain.post.service.trending.TrendingTagQueryService;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagRestController implements TagControllerDocs{
    private final TrendingTagQueryService trendingTagQueryService;

    @Override
    @GetMapping("/trending")
    public ApiResponse<List<TagResponseDTO.TrendingTags>> getTrendingTags(

    ){
        return ApiResponse.onSuccess(trendingTagQueryService.getTrendingTags());
    }

}
