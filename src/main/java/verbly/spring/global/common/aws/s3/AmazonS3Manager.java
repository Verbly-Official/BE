package verbly.spring.global.common.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import verbly.spring.domain.uuid.entity.Uuid;
import verbly.spring.global.config.AmazonConfig;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {
    private final AmazonS3 amazonS3;
    private final AmazonConfig amazonConfig;

    public String uploadFile(String keyName, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType()); // aws의 s3의 폴더의 파일에서 다운로드 말고 브라우저에서 사진 확인 가능
        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
        } catch (IOException e){
            log.error("error at AmazonS3Manager uploadFile : {}", (Object) e.getStackTrace());
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    public String generateUserKeyName(Uuid uuid) {
        return amazonConfig.getUserPath() + '/' + uuid.getUuid(); // 멤버 프로필 사진
    }

    public String extractS3KeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        String bucketUrlPrefix = "https://" + amazonConfig.getBucket() + ".s3." + amazonConfig.getRegion() + ".amazonaws.com/";

        if (!imageUrl.startsWith(bucketUrlPrefix)) {
            log.info("S3가 아닌 외부 프로필 URL이므로 삭제 대상에서 제외합니다. url={}", imageUrl);
            return null;
        } //else {
//            throw new IllegalArgumentException("S3 URL에서 key를 추출할 수 없습니다: " + imageUrl);
//        }
        return imageUrl.substring(bucketUrlPrefix.length());
    }

    public void deleteFile(String keyName) {
        amazonS3.deleteObject(amazonConfig.getBucket(), keyName);
    }
}
