package verbly.spring.domain.post.controller.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.global.common.response.ApiResponse;
import verbly.spring.global.security.auth.CustomUserDetails;

import java.util.UUID;

@Tag(name = "Post", description = "게시글 및 댓글 관련 API")
public interface PostControllerDocs {

    @Operation(
            summary = "홈 화면 포스트 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "홈 화면에서 게시글 목록을 무한 스크롤(Slice) 방식으로 조회합니다.\n\n" +
                    "✅ **요청 파라미터 (Query String):**\n" +
                    "- page: 페이지 번호 (Integer, 0부터 시작)\n" +
                    "- size: 페이지 크기 (Integer, 기본 10)\n" +
                    "- sort: 정렬 기준 (String, 기본 createdAt, DESC)\n"
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
                                                "content": [
                                                  {
                                                    "userImageUrl": "http://k.kakaocdn.net/dn/cZzX2j/btsLx7TX3Gx/QMDTWrOkMpg46aWkufLcQ1/img_640x640.jpg",
                                                    "nickname": "박시윤",
                                                    "isFollowing": false,
                                                    "uuid": "00000000-0000-0000-0000-000000000000",
                                                    "postId": 1,
                                                    "content": "string",
                                                    "status": "PENDING",
                                                    "likesCount": 0,
                                                    "commentsCount": 0,
                                                    "createdAt": "2026-02-03T17:17:24.14421",
                                                    "tags": [
                                                      "string"
                                                    ],
                                                    "isLiked": false
                                                  }
                                                ],
                                                "pageable": {
                                                  "pageNumber": 0,
                                                  "pageSize": 1,
                                                  "sort": {
                                                    "empty": false,
                                                    "sorted": true,
                                                    "unsorted": false
                                                  },
                                                  "offset": 0,
                                                  "paged": true,
                                                  "unpaged": false
                                                },
                                                "last": true,
                                                "totalElements": 1,
                                                "totalPages": 1,
                                                "size": 1,
                                                "number": 0,
                                                "sort": {
                                                  "empty": false,
                                                  "sorted": true,
                                                  "unsorted": false
                                                },
                                                "first": true,
                                                "numberOfElements": 1,
                                                "empty": false
                                              }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<Slice<PostResponseDTO.HomePosts>> getHomePosts(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation( summary = "특정 유저 포스트 조회",
                security = @SecurityRequirement(name = "JWT TOKEN"),
                description = "홈 화면에서 게시글 목록을 무한 스크롤(Slice) 방식으로 조회합니다.\n\n" +
                    "✅ **요청 파라미터 (Query String):**\n" +
                    "- page: 페이지 번호 (Integer, 0부터 시작)\n" +
                    "- size: 페이지 크기 (Integer, 기본 10)\n" +
                    "- sort: 정렬 기준 (String, 기본 createdAt, DESC)\n\n" +
                    "✅ **path variable:**\n" +
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
                                                    "content": [
                                                      {
                                                        "userImageUrl": "http://k.kakaocdn.net/dn/cZzX2j/btsLx7TX3Gx/QMDTWrOkMpg46aWkufLcQ1/img_640x640.jpg",
                                                        "nickname": "박시윤",
                                                        "isFollowing": false,
                                                        "uuid": "00000000-0000-0000-0000-000000000000",
                                                        "postId": 1,
                                                        "content": "string",
                                                        "status": "PENDING",
                                                        "likesCount": 0,
                                                        "commentsCount": 0,
                                                        "createdAt": "2026-02-03T17:17:24.14421",
                                                        "tags": [
                                                          "string"
                                                        ],
                                                        "isLiked": false
                                                      }
                                                    ],
                                                    "pageable": {
                                                      "pageNumber": 0,
                                                      "pageSize": 1,
                                                      "sort": {
                                                        "empty": false,
                                                        "sorted": true,
                                                        "unsorted": false
                                                      },
                                                      "offset": 0,
                                                      "paged": true,
                                                      "unpaged": false
                                                    },
                                                    "size": 1,
                                                    "number": 0,
                                                    "sort": {
                                                      "empty": false,
                                                      "sorted": true,
                                                      "unsorted": false
                                                    },
                                                    "first": true,
                                                    "last": true,
                                                    "numberOfElements": 1,
                                                    "empty": false
                                                  }
                                                }
                                        """
                            )
                    )
            )
    })
    ApiResponse<Slice<PostResponseDTO.UserPosts>> getUserPosts(
            Pageable pageable,
            UUID uuid,
            CustomUserDetails userDetails
    );

    @Operation( summary = "특정 포스트에 좋아요 추가",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 포스트에 좋아요 추가\n\n" +
                    "✅ **path variable:**\n" +
                    "- postId: 해당 포스트 ID (Long)\n"
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
                                                    "postId": 1,
                                                    "likesCount": 1,
                                                    "isLiked": true
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<PostResponseDTO.AddPostLike> addPostLike(
            Long postId,
            CustomUserDetails userDetails
    );

    @Operation( summary = "특정 포스트에 좋아요 제거",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 포스트에 좋아요 제거\n\n" +
                    "✅ **path variable:**\n" +
                    "- postId: 해당 포스트 ID (Long)\n"
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
                                                    "postId": 1,
                                                    "likesCount": 0,
                                                    "isLiked": false
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<PostResponseDTO.AddPostLike> deletePostLike(
            Long postId,
            CustomUserDetails userDetails
    );

    @Operation( summary = "특정 포스트 댓글 조회",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 포스트의 댓글 목록을 무한 스크롤(Slice) 방식으로 조회합니다.\n\n" +
                    "✅ **요청 파라미터 (Query String):**\n" +
                    "- page: 페이지 번호 (Integer, 0부터 시작)\n" +
                    "- size: 페이지 크기 (Integer, 기본 10)\n" +
                    "- sort: 정렬 기준 (String, 기본 createdAt, DESC)\n\n" +
                    "✅ **path variable:**\n" +
                    "- postId: 해당 포스트 Id (Long)\n"
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
                                                    "content": [
                                                      {
                                                        "userImageUrl": "http://k.kakaocdn.net/dn/cZzX2j/btsLx7TX3Gx/QMDTWrOkMpg46aWkufLcQ1/img_640x640.jpg",
                                                        "nickname": "박시윤",
                                                        "isFollowing": false,
                                                        "uuid": "00000000-0000-0000-0000-000000000000",
                                                        "postId": 1,
                                                        "content": "string",
                                                        "status": "PENDING",
                                                        "likesCount": 0,
                                                        "commentsCount": 0,
                                                        "createdAt": "2026-02-03T17:17:24.14421",
                                                        "tags": [
                                                          "string"
                                                        ],
                                                        "isLiked": false
                                                      }
                                                    ],
                                                    "pageable": {
                                                      "pageNumber": 0,
                                                      "pageSize": 1,
                                                      "sort": {
                                                        "empty": false,
                                                        "sorted": true,
                                                        "unsorted": false
                                                      },
                                                      "offset": 0,
                                                      "paged": true,
                                                      "unpaged": false
                                                    },
                                                    "size": 1,
                                                    "number": 0,
                                                    "sort": {
                                                      "empty": false,
                                                      "sorted": true,
                                                      "unsorted": false
                                                    },
                                                    "first": true,
                                                    "last": true,
                                                    "numberOfElements": 1,
                                                    "empty": false
                                                  }
                                                }
                                        """
                            )
                    )
            )
    })
    ApiResponse<Slice<CommentResponseDTO.getComment>> getComments(
            Pageable pageable,
            Long postId
    );

    @Operation(
            summary = "특정 포스트에 댓글 작성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "특정 포스트에 댓글 작성\n\n" +
                    "✅ **Path Variable:**\n" +
                    "- postId: 해당 포스트 Id (Long)\n",
            requestBody = @RequestBody(
                    description = "댓글 작성 요청 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "댓글 작성 예시", // Dropdown에 표시될 이름
                                            value = """
                                                    {
                                                        "content" : "작성할 댓글 내용입니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
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
                                                    "uuid": "00000000-0000-0000-0000-000000000000",
                                                    "nickname": "박시윤",
                                                    "userImageUrl": "http://k.kakaocdn.net/dn/cZzX2j/btsLx7TX3Gx/QMDTWrOkMpg46aWkufLcQ1/img_640x640.jpg",
                                                    "content": "작성할 댓글 내용입니다.",
                                                    "createdAt": "2026-02-03T18:51:39.413782",
                                                    "totalComments": 1
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<CommentResponseDTO.getMyComment> makeComment(
            CustomUserDetails userDetails,
            Long postId,
            CommentRequestDTO.makeComment dto
    );


    @Operation(
            summary = "홈 화면 포스트 작성",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "홈 화면에서 작성 가능한 포스트\n\n" +
                    "✅ **Path Variable:**\n" +
                    "- postId: 해당 포스트 Id (Long)\n",
            requestBody = @RequestBody(
                    description = "포스트 작성 예시",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "포스트 작성 예시",
                                            value = """
                                                    {
                                                        "content" : "포스트에 작성할 내용입니다.",
                                                        "publicSetting" : true,
                                                        "tags" : ["tag1", "tag2"]
                                                    }
                                                    """
                                    )
                            }
                    )
            )
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
                                                    "postId": 2,
                                                    "createdAt": "2026-02-03T18:49:35.5327692"
                                                }
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<PostResponseDTO.HomeWritePost> writeHomePost(
            CustomUserDetails userDetails,
            PostRequestDTO.HomeWritePost dto
    );
}


