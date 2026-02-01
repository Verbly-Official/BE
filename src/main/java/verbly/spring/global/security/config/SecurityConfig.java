package verbly.spring.global.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import verbly.spring.global.security.handler.CustomAuthenticationEntryPoint;
import verbly.spring.global.security.jwt.JwtAuthenticationFilter;
import verbly.spring.global.security.jwt.JwtTokenProvider;
import verbly.spring.global.security.oauth.handler.OAuth2FailureHandler;
import verbly.spring.global.security.oauth.handler.OAuth2SuccessHandler;
import verbly.spring.global.security.oauth.service.CustomOAuth2UserService;
import verbly.spring.global.security.oauth.service.CustomOidcUserService;

import java.util.List;

@EnableWebSecurity // Spring Security 설정을 활성화시키는 역할 -> 따라서 우리가 직접 작성한 보안 설정이 Spring Security의 기본 설정보다 우선 적용되게 됨
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    CorsConfigurationSource corsConfigurationSource() { // CORS 설정
        CorsConfiguration config = new CorsConfiguration();

        // 버블리 프론트, 백엔드 로컬, 운영 도메인 등 실제 사용하는 도메인 입력
        config.setAllowedOrigins(List.of(
                "https://www.verbly.site", // 프론트 도메인
                "https://api.verbly.site", // 백엔드 도메인
                "http://3.36.90.173:3000",
                "http://3.36.90.173:8080",
                "http://localhost:3000", // 로컬 프론트
                "http://localhost:8080", // 로컬 백엔드
                "http://localhost:5173" // 로컬 프론트
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));  // JWT 토큰 읽기 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(HttpBasicConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용 시 STATELESS
                )
                .authorizeHttpRequests(
                        (requests) -> requests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/", "/api/auth/reissue", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/actuator/**").hasRole("ADMIN")
//                                .requestMatchers("/admin/**").hasRole("ADMIN") // pm이 ADMIN역할 기능 필요 X
                                .anyRequest().authenticated()
                )
//                .csrf()
//                .disable()
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService) // 구글
                                .userService(customOAuth2UserService) // 카카오, 네이버
                        )
                        .successHandler(oAuth2SuccessHandler) // 온보딩 분기 등 커스텀 성공 핸들러
                        .failureHandler(oAuth2FailureHandler)
                )
                .exceptionHandling(exception -> exception
                                .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패 처리 (401)
                )
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}
