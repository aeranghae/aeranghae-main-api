package cloud.aeranghae.main.repository;

import cloud.aeranghae.main.domain.Project;
import cloud.aeranghae.main.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 🌟 특정 사용자의 프로젝트만 최신순으로 조회
    List<Project> findAllByUserOrderByCreatedAtDesc(User user);
}