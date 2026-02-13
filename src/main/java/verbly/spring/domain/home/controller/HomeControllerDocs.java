package verbly.spring.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import verbly.spring.domain.home.dto.response.HomeResponseDTO;
import verbly.spring.global.common.response.ApiResponse;

import java.util.UUID;

@Tag(name = "Home-User", description = "홈 화면 유저 관련 API")
public interface HomeControllerDocs {

    @Operation(
            summary = "홈 화면 유저 정보 조회",
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
                                                "result": {
                                                    "imageUrl": "http://...",
                                                    "nickname": "박시윤",
                                                    "nativeLang": "kr",
                                                    "following": 0,
                                                    "streak": 0,
                                                    "point": 100,
                                                    "correctionReceived": 0,
                                                    "level": "LV1"
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<HomeResponseDTO.HomeViewerInfoDTO> getHomeViewerInfo(
    );

    @Operation(
            summary = "특정 유저 프로필 확인 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "✅ **path variable:**\n" +
                          "- uuid: 상대 유저 uuid (UUID)\n"
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
                                                "result": {
                                                    "imageUrl": "http://...",
                                                    "nickname": "aa",
                                                    "nativeLang": "KO",
                                                    "description": "asdf",
                                                    "totalPosts": 2,
                                                    "follower": 0,
                                                    "following": 1,
                                                    "isFollowing": false,
                                                    "correctionReceived": 0,
                                                    "correctionGiven": 0
                                                    "userId": 33
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<HomeResponseDTO.HomeUserInfoDTO> getUserProfileInfo(
            @PathVariable(name = "uuid") UUID uuid
    );

    @Operation(
            summary = "출석 확인 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "✅ **query string:**\n" +
                    "- timezone: 사용자 위치 timezone (String/ 부적절한 timezone 입력 시 seoul로 고정)\n"
    )
    ResponseEntity<Void> homeApi(
            @RequestParam String timezone
    );
}
