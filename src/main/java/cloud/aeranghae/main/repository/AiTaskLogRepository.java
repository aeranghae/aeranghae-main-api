package cloud.aeranghae.main.repository;

import cloud.aeranghae.main.domain.AiTaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiTaskLogRepository extends JpaRepository<AiTaskLog, Long> {
}