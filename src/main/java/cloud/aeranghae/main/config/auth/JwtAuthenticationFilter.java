package cloud.aeranghae.main.config.auth;

import cloud.aeranghae.main.domain.User;
import cloud.aeranghae.main.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository; // 🌟 활동 시간 체크를 위해 추가

    @Override
    @Transactional // 🌟 활동 시간을 DB에 바로 반영하기 위해 추가
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUserEmail(token);

            // 3. 🌟 [추가됨] DB에서 유저 조회 및 활동 시간 체크
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                // 30분 지났는지 확인
                if (user.isTimedOut(30)) {
                    // 30분 지났으면 도장 안 찍어주고 401 에러 반환
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"30분간 활동이 없어 로그아웃되었습니다.\"}");
                    return;
                }

                // 4. 🌟 [추가됨] 아직 유효하다면 활동 시간 갱신 (30분 연장)
                user.updateActivity();

                // 5. 스프링 시큐리티 인증 처리
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. 다음 단계로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}