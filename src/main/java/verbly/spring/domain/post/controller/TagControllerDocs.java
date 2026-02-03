package verbly.spring.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import verbly.spring.domain.post.dto.response.TagResponseDTO;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;

@Tag(name = "Tag", description = "태그 관련 API")
public interface TagControllerDocs {

    @Operation(
            summary = "트랜딩 태그 조회",
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                           {
                                                "isSuccess": true,
                                                "code": "COMMON2000",
                                                "message": "성공입니다.",
                                                "result": [
                                                    {
                                                        "tagId": 1,
                                                        "tagName": "string",
                                                        "count": 1,
                                                        "ranking": 1
                                                    }
                                                ]
                                           }
                                        """
                            )
                    )
            )
    })
    ApiResponse<List<TagResponseDTO.TrendingTags>> getTrendingTags(

    );
}
