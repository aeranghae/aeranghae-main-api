package cloud.aeranghae.main.config.auth;

import cloud.aeranghae.main.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(authorize -> authorize
                        // 로그인 화면 및 기본 리소스는 모두 접근 허용
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/login/**", "/oauth2/**").permitAll()
                        // api/projects 주소는 USER 권한을 가진 사람만 접근 가능하도록 설정
                        .requestMatchers("/api/projects/**").hasRole(Role.USER.name())
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }
}