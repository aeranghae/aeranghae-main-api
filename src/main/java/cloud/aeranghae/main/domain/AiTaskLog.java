package cloud.aeranghae.main.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_tasks")
public class AiTaskLog {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private AiTask aiTask;

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] compressedContent; // 여기에 LZ4로 압축된 바이트가 들어갑니다.
}