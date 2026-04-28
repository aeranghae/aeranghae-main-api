package cloud.aeranghae.main.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 구글에서 가져온 실제 이름

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String picture;

    @Column
    private String nickname; // 서비스 내 별명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 🌟 추가된 필드: 마지막 활동 시간을 기록합니다.
    @Column
    private LocalDateTime lastActivityAt;

    @Builder
    public User(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.lastActivityAt = LocalDateTime.now(); // 가입 시 초기화
    }

    // 구글 로그인 시 정보를 최신화하는 메서드
    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        this.updateActivity(); // 로그인도 활동이므로 시간 갱신
        return this;
    }

    // 닉네임 설정 및 정식 유저 승급 메서드
    public void authorizeUser(String nickname) {
        this.nickname = nickname;
        this.role = Role.USER;
        this.updateActivity(); // 활동 시간 갱신
    }

    // 🌟 활동 시간 갱신: API 요청이 들어올 때마다 호출해서 30분을 연장합니다.
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    // 🌟 타임아웃 체크: 마지막 활동으로부터 입력받은 시간(분)이 지났는지 확인합니다.
    public boolean isTimedOut(int minutes) {
        if (this.lastActivityAt == null) return true; // 기록이 없으면 만료로 간주
        long elapsedMinutes = Duration.between(this.lastActivityAt, LocalDateTime.now()).toMinutes();
        return elapsedMinutes >= minutes;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Role {
        GUEST("ROLE_GUEST", "손님"),
        USER("ROLE_USER", "일반 사용자");

        private final String key;
        private final String title;
    }
}