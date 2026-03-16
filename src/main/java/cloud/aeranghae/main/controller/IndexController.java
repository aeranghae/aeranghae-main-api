package cloud.aeranghae.main.controller;

import cloud.aeranghae.main.config.auth.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model) {
        // CustomOAuth2UserService에서 로그인 성공 시 세션에 저장한 유저 정보 가져오기
        SessionUser user = (SessionUser) httpSession.getAttribute("user");

        // 세션에 유저 정보가 있다면 모델에 담아서 타임리프로 전달
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userRole", user.getRole()); // 화면에 권한 정보도 넘겨줌
        }
        return "index"; // src/main/resources/templates/index.html 을 찾아서 반환
    }
}