package cloud.aeranghae.main.service;

import cloud.aeranghae.main.domain.User;
import cloud.aeranghae.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void signup(String email, String nickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 엔티티의 승급 메서드 호출 (JPA 더티 체킹으로 자동 DB 업데이트)
        user.authorizeUser(nickname);
    }
}