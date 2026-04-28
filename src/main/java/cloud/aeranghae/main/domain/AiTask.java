package cloud.aeranghae.main.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_tasks")
public class AiTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟 이 작업이 어떤 프로젝트 소속인지 알아야 합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // 🌟 작업의 현재 상태 (PENDING, COMPLETED, FAILED)
    private String status;

    // 🌟 사용자가 보낸 질문 (이건 목록에서 보여줘야 하니까 L1에 둡니다)
    @Column(columnDefinition = "TEXT")
    private String prompt;

    // 🌟 생성 시간 (언제 시켰는지 알아야죠!)
    private LocalDateTime createdAt;
}