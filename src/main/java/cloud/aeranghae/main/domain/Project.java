package cloud.aeranghae.main.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "projects")
@EntityListeners(AuditingEntityListener.class) // 생성/수정 시간 자동 기록용
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟 어떤 사용자의 프로젝트인지 연결 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String repositoryUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Project(User user, String title, String description, String repositoryUrl) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.repositoryUrl = repositoryUrl;
    }

    // 프로젝트 정보 수정 메서드
    public void update(String title, String description, String repositoryUrl) {
        this.title = title;
        this.description = description;
        this.repositoryUrl = repositoryUrl;
    }
}