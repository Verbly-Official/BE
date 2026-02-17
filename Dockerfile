# 1. Java 17 런타임 기반 이미지 사용
FROM eclipse-temurin:17-jre

# 2. 컨테이너 내부 작업 디렉토리 생성
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너 안으로 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너가 8080 포트를 사용함을 명시
EXPOSE 8080

# 5. 컨테이너 실행 시 Spring Boot 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]