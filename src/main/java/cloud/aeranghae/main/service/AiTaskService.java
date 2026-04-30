package cloud.aeranghae.main.service;

import cloud.aeranghae.main.domain.AiTask;
import cloud.aeranghae.main.domain.AiTaskLog;
import cloud.aeranghae.main.repository.AiTaskLogRepository;
import cloud.aeranghae.main.repository.AiTaskRepository;
import cloud.aeranghae.main.repository.ProjectRepository;
import cloud.aeranghae.main.util.Lz4Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiTaskService {
    // 🌟 변수명 소문자로 통일
    private final AiTaskRepository aiTaskRepository;
    private final AiTaskLogRepository aiTaskLogRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public Long saveTask(Long projectId, String prompt, String aiResponse) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        // 1. AiTask 생성 (L1)
        AiTask task = AiTask.builder()
                .project(project)
                .taskType("CODE_ANALYSIS")
                .prompt(prompt)
                .build();
        task.complete("L2에 저장됨"); // 🌟 AiTask에 이 메서드가 있어야 함
        aiTaskRepository.save(task);

        // 2. LZ4 압축 (L2 저장용)
        byte[] compressedData = Lz4Util.compress(aiResponse);

        // 3. AiTaskLog 생성 (L2)
        AiTaskLog taskLog = AiTaskLog.builder()
                .aiTask(task)
                .compressedContent(compressedData)
                .build();
        aiTaskLogRepository.save(taskLog);

        return task.getId();
    }

    @Transactional(readOnly = true)
    public String getTaskDetail(Long taskId) {
        AiTaskLog log = aiTaskLogRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("로그 없음"));

        return Lz4Util.decompress(log.getCompressedContent());
    }
}