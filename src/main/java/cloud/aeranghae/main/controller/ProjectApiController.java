package cloud.aeranghae.main.controller;

import cloud.aeranghae.main.controller.dto.ProjectSaveRequestDto;
import cloud.aeranghae.main.domain.Project;
import cloud.aeranghae.main.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 클라이언트에서 보낸 HTTP 요청을 먼저 받아 서비스에 수행 요청
@RequiredArgsConstructor // 생성자 주입 자동화
@RestController // 1. JSON 데이터를 주고받는 API 전용 컨트롤러임을 선언합니다.
@RequestMapping("/api/projects") // 2. 이 컨트롤러의 모든 API는 /api/projects 주소로 시작합니다.
public class ProjectApiController {

    private final ProjectService projectService;

    /**
     * 🌟 프로젝트 생성 API (POST /api/projects)
     */
    @PostMapping
    public Long createProject(@RequestBody ProjectSaveRequestDto requestDto, // 3. 리액트가 보낸 JSON 데이터를 DTO 객체로 변환해서 받습니다.
                              @AuthenticationPrincipal String email) { // 4. ⭐핵심! JWT 필터가 넣어준 인증 정보(이메일)를 여기서 쏙 꺼내 씁니다.

        // 5. 서비스에게 프로젝트 저장을 시킵니다.
        return projectService.save(email, requestDto.getTitle(), requestDto.getDescription(), requestDto.getRepositoryUrl());
    }

    /**
     * 🌟 내 프로젝트 목록 가져오기 API (GET /api/projects)
     */
    @GetMapping
    public List<Project> getMyProjects(@AuthenticationPrincipal String email) {
        // 6. 로그인한 사람의 이메일만 서비스로 넘겨서 그 사람의 프로젝트만 가져옵니다.
        return projectService.findAllByEmail(email);
    }
}
