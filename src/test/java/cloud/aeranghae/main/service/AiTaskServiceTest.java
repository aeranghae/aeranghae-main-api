package cloud.aeranghae.main.service;

import cloud.aeranghae.main.domain.AiTask;
import cloud.aeranghae.main.domain.Project;
import cloud.aeranghae.main.domain.User;
import cloud.aeranghae.main.repository.AiTaskRepository;
import cloud.aeranghae.main.repository.ProjectRepository;
import cloud.aeranghae.main.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 후 DB 데이터를 롤백하여 깔끔하게 유지합니다.
class AiTaskServiceTest {

    @Autowired private AiTaskService aiTaskService;
    @Autowired private AiTaskRepository aiTaskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("LLM JSON 응답을 L1에 요약 저장하고 L2에 압축 저장한다")
    void saveAndDecompressTest() {
        // 0. 사용자 정의
        User user = User.builder()
                .email("test@test.com")
                .name("test")
                .role(User.Role.USER)
                .build();
        userRepository.save(user); // 유저 먼저 저장

        // 1. GIVEN: 가짜 프로젝트와 LLM JSON 응답 준비
        Project project = Project.builder()
                .title("테스트 프로젝트")
                .user(user)
                .build();
        projectRepository.save(project);

        String mockPrompt = "테스트 최적화중";
        String mockJsonResponse = """
            {
              "title": "Java Stream Test",
              "summary": "테스트 내용입니다.",
              "content": "테스트 내용입니다(LZ4로 압축될 예정)"
            }
            """;

        // 2. WHEN: 서비스 호출 (저장 로직 가동)
        Long taskId = aiTaskService.saveTask(project.getId(), mockPrompt, mockJsonResponse);

        // 3. THEN: L1(AiTask) 검증
        AiTask savedTask = aiTaskRepository.findById(taskId).get();

        // 제목과 요약이 JSON에서 잘 파싱되어 들어갔는지 확인
        assertThat(savedTask.getSummary()).contains("json Stream Test"); //
        assertThat(savedTask.getSummary()).contains("json Stream Test");
        assertThat(savedTask.getTaskType()).isEqualTo("LLM_json_task");

        // 4. THEN: L2(AiTaskLog) 압축 해제 검증
        // 저장된 압축 데이터를 다시 풀어서 원본과 비교
        String decompressedDetail = aiTaskService.getTaskDetail(taskId);

        assertThat(decompressedDetail).isEqualTo(mockJsonResponse);
        System.out.println("압축 해제 결과: " + decompressedDetail);
    }
}