package cloud.aeranghae.main.service;

import cloud.aeranghae.main.domain.Project;
import cloud.aeranghae.main.domain.User;
import cloud.aeranghae.main.repository.ProjectRepository;
import cloud.aeranghae.main.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor // 1. final이 붙은 필드(레포지토리 등)를 모아 생성자를 자동으로 만들어줍니다. (의존성 주입)
@Service // 2. 이 클래스를 스프링 서비스 빈으로 등록합니다.
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 🌟 새 프로젝트 저장 로직
     */
    @Transactional // 3. 이 메서드 안의 작업은 하나의 '트랜잭션'으로 묶입니다. (하나라도 실패하면 모두 취소!)
    public Long save(String email, String title, String description, String repoUrl) {

        // 4. 토큰에서 추출한 이메일로 DB에서 실제 유저 정보를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. email=" + email));

        // 5. 빌더 패턴을 사용해 프로젝트 객체를 생성하고, 위에서 찾은 유저(주인)를 연결합니다.
        Project project = Project.builder()
                .user(user)
                .title(title)
                .description(description)
                .repositoryUrl(repoUrl)
                .build();

        // 6. DB에 저장하고, 생성된 프로젝트의 ID(PK)를 반환합니다.
        return projectRepository.save(project).getId();
    }

    /**
     * 🌟 로그인한 유저의 프로젝트 목록 조회
     */
    @Transactional(readOnly = true) // 7. 조회 전용 모드입니다. (성능이 약간 더 빨라지고 데이터 변경을 방지합니다.)
    public List<Project> findAllByEmail(String email) {

        // 8. 먼저 이메일로 유저를 찾고,
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. email=" + email));

        // 9. 레포지토리에 미리 만들어둔 '유저별 조회' 메서드를 호출해 목록을 가져옵니다.
        return projectRepository.findAllByUserOrderByCreatedAtDesc(user);
    }
}