package verbly.spring.global.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import verbly.spring.global.common.code.BaseErrorCode;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.common.exception.BaseException;
import verbly.spring.global.common.response.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
//    @ExceptionHandler
//    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
//        String errorMessage = e.getConstraintViolations().stream()
//                .map(constraintViolation -> constraintViolation.getMessage())
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));
//
//        return handleExceptionInternalConstraint(e, ErrorStatus.valueOf(errorMessage), HttpHeaders.EMPTY,request);
//    }
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();

    e.getConstraintViolations().forEach(violation -> {
        String fieldName = violation.getPropertyPath().toString();
        if (fieldName.contains(".")) {
            fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
        }
        String errorMessage = violation.getMessage();
        errors.put(fieldName, errorMessage);
    });

    // 로그로 상세 에러 출력
    log.warn("Validation 실패: {}", errors);

    return handleExceptionInternalConstraint(e, ErrorStatus._BAD_REQUEST, HttpHeaders.EMPTY, request);
}

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().stream()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(e,HttpHeaders.EMPTY,ErrorStatus.valueOf("_BAD_REQUEST"),request,errors);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();

        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(),request, e.getMessage());
    }

    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity<Object> onThrowException(BaseException generalException, HttpServletRequest request) {
        return handleExceptionInternal(generalException, generalException.getCode(), HttpHeaders.EMPTY, request);
    }


    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String requestUri = "";
        if (request instanceof ServletWebRequest servletWebRequest) {
            requestUri = servletWebRequest.getRequest().getRequestURI();
        }

        log.error("📦 [이미지 업로드 용량 초과]: {} | 요청 URI: {}", e.getMessage(), requestUri);

        ErrorStatus errorStatus;

        if (requestUri.contains("/user/profile-image")) {
            errorStatus = ErrorStatus.IMAGE_FILE_TOO_LARGE; // 유저 프로필 이미지 업로드 실패 시
        } //else if (requestUri.contains("/article") || requestUri.contains("/articles")) {
            //errorStatus = ErrorStatus.ARTICLE_PHOTO_IMAGE_TOO_LARGE; // 게시글 이미지 업로드 실패 시
        //}
        else {
            errorStatus = ErrorStatus._BAD_REQUEST; // 그 외 요청
        }

        ResponseEntity<Object> response = handleExceptionInternalFalse(
                e,
                errorStatus,
                HttpHeaders.EMPTY,
                errorStatus.getHttpStatus(),
                request,
                e.getMessage()
        );

        log.error("📦 [이미지 업로드 용량 초과 응답]: status={}, body={}", response.getStatusCode(), response.getBody());
        return response;
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, BaseErrorCode errorCode,
                                                           HttpHeaders headers, HttpServletRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(errorCode);

        WebRequest webRequest = new ServletWebRequest(request);

        HttpHeaders safeHeaders = (headers == null) ? HttpHeaders.EMPTY : headers;

        HttpStatus status = errorCode.getReasonHttpStatus().getHttpStatus();
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCode.getReasonHttpStatus().getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorStatus errorCommonStatus,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, ErrorStatus errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, ErrorStatus errorCommonStatus,
                                                                     HttpHeaders headers, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }
}
