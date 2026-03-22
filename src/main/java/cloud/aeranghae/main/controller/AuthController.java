package cloud.aeranghae.main.controller;

import cloud.aeranghae.main.config.auth.JwtTokenProvider; // 추가됨!
import cloud.aeranghae.main.domain.User;
import cloud.aeranghae.main.config.auth.dto.GoogleLoginRequest;
import cloud.aeranghae.main.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtTokenProvider jwtTokenProvider; // 추가됨!

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        try {
            // 1. 구글 토큰 검증 및 유저 DB 저장/업데이트
            User user = googleAuthService.verifyTokenAndLogin(request.getCredential());

            // 2. 우리 서버만의 JWT 토큰 생성!
            String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());

            // 3. 리액트가 쓰기 편하게 JSON(Map) 형태로 묶어서 반환
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("picture", user.getPicture());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("로그인 실패: " + e.getMessage());
        }
    }
}