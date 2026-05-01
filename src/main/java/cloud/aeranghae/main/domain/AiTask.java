package cloud.aeranghae.main.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "ai_tasks")
@EntityListeners(AuditingEntityListener.class)
public class AiTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String taskType;
    @Column(columnDefinition = "TEXT")
    private String prompt;
    private String status;
    private String summary;
    private String build;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 🌟 서비스에서 사용하는 핵심 메서드
    public void complete(String summary) {
        this.status = "COMPLETED";
        this.summary = summary;
    }
}