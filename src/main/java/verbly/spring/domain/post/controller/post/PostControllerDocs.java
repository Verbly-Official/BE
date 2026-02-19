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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import verbly.spring.domain.post.dto.request.CommentRequestDTO;
import verbly.spring.domain.post.dto.request.PostRequestDTO;
import verbly.spring.domain.post.dto.response.CommentResponseDTO;
import verbly.spring.domain.post.dto.response.PostResponseDTO;
import verbly.spring.global.common.response.ApiResponse;

import java.util.List;
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
            Pageable pageable
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
                                                        "userId": 33,
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
            UUID uuid
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
            Long postId
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
            Long postId
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
            PostRequestDTO.HomeWritePost dto
    );

    @Operation( summary = "핫 포스트 리스트 반환 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "핫 포스트 리스트 반환\n\n"
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
                                                       "userImageUrl": "http://k.kakaocdn.net/dn/cZzX2j/btsLx7TX3Gx/QMDTWrOkMpg46aWkufLcQ1/img_640x640.jpg",
                                                       "nickname": "박시윤",
                                                       "isFollowing": false,
                                                       "uuid": "f44c7ef2-1ee4-48db-ad8c-aa7d7884e160",
                                                       "postId": 1,
                                                       "content": "내용",
                                                       "status": "COMPLETED",
                                                       "likesCount": 100,
                                                       "commentsCount": 0,
                                                       "createdAt": "2026-02-03T23:26:46.038337",
                                                       "tags": [],
                                                       "isLiked": false
                                                    }
                                                ]
                                            }
                                        """
                            )
                    )
            )
    })
    ApiResponse<List<PostResponseDTO.hotPost>> getHotPosts();


    @Operation( summary = "포스트 검색 API",
            security = @SecurityRequirement(name = "JWT TOKEN"),
            description = "검색한 포스트를 무한 스크롤(Slice) 방식으로 조회합니다.\n\n" +
                    "✅ **요청 파라미터 (Query String):**\n" +
                    "- page: 페이지 번호 (Integer, 0부터 시작)\n" +
                    "- size: 페이지 크기 (Integer, 기본 10)\n" +
                    "- sort: 정렬 기준 (String, 기본 createdAt, DESC)\n\n" +
                    "✅ **path variable:**\n" +
                    "- keyword: 검색 키워드 (String)\n" +
                    "- Tag 검색: #을 포함한 Keyword ( #String )\n" +
                    "- Content 검색: #없이 ( String )\n" +
                    "- MySQL 성능 및 기술 이슈로 'am'과 같은 의미가 적은 단어는 검색 X"
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
                                                              "uuid": "26b5e7a5-b76d-408c-89ae-fbd76f2a74ba",
                                                              "userId": 2,
                                                              "postId": 1,
                                                              "content": "I am working on the new feature since two weeks and it still not finished.\\nMy manager said me to submit it until Friday, but I didn’t started yet.\\nThe meeting was cancelled because nobody didn’t join.\\nI will send you the document when I will finish it.",
                                                              "status": "COMPLETED",
                                                              "likesCount": 0,
                                                              "commentsCount": 0,
                                                              "createdAt": "2026-02-12T04:19:16.163934",
                                                              "tags": [
                                                                "#Work",
                                                                "#Update"
                                                              ],
                                                              "isLiked": false
                                                            }
                                                          ],
                                                          "pageable": {
                                                            "pageNumber": 0,
                                                            "pageSize": 2,
                                                            "sort": {
                                                              "empty": false,
                                                              "sorted": true,
                                                              "unsorted": false
                                                            },
                                                            "offset": 0,
                                                            "paged": true,
                                                            "unpaged": false
                                                          },
                                                          "size": 2,
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
    ApiResponse<Slice<PostResponseDTO.HomePosts>> searchPosts(
            @PathVariable String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );
}


