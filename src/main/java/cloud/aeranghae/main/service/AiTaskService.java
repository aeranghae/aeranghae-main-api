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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class AiTaskService {
    private final AiTaskRepository aiTaskRepository;
    private final AiTaskLogRepository aiTaskLogRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Long saveTask(Long projectId, String prompt, String aiResponse) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        String extractedTitle = "제목 X";
        String extractedSummary = "요약 정보 X";

        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            extractedTitle = root.path("title").asText("제목 없음");
            extractedSummary = root.path("summary").asText("내용 요약 실패");
        } catch (Exception e) {
            // JSON 파싱 실패 시 원본의 일부라도 요약으로 사용
            extractedSummary = aiResponse.substring(0, Math.min(aiResponse.length(), 50)) + "...";
        }
        // 1. AiTask 생성 (L1)
        AiTask task = AiTask.builder()
                .project(project)
                .taskType("LLM_json_task")
                .prompt(prompt)
                .status("저장 완료")
                .build();

        task.complete("[" + extractedTitle + "] " + extractedSummary); // 🌟 AiTask에 이 메서드가 있어야 함
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