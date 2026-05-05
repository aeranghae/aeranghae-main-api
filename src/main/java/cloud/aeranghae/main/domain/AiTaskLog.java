package cloud.aeranghae.main.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "ai_tasks")
public class AiTaskLog {
    @Id
    private Long id; // AiTask의 ID와 매핑함

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 🌟 AiTask의 ID를 PK로 사용하도록 연결
    @JoinColumn(name = "project_id")
    private AiTask aiTask;

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] compressedContent;
}