package cloud.aeranghae.main.controller;

import cloud.aeranghae.main.config.auth.dto.SessionUser;
import cloud.aeranghae.main.controller.dto.SignupRequestDto;
import cloud.aeranghae.main.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;
    private final HttpSession httpSession;

    @PostMapping("/api/user/signup")
    public String signup(@RequestBody SignupRequestDto requestDto) {
        // 세션에서 현재 로그인한 유저의 이메일을 가져옴
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user != null) {
            userService.signup(user.getEmail(), requestDto.getNickname());
        }
        return "success";
    }
}