package cloud.aeranghae.main.repository;

import cloud.aeranghae.main.domain.AiTask;
import cloud.aeranghae.main.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiTaskRepository extends JpaRepository<AiTask, Long> {
    // AiTask -> Project -> User를 타고 올라가서 특정 유저의 작업만 가져옵니다.
    List<AiTask> findAllByProject_UserOrderByCreatedAtDesc(User user);
}