package cloud.aeranghae.main.controller;

import cloud.aeranghae.main.controller.dto.SignupRequestDto;
import cloud.aeranghae.main.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;

    @PostMapping("/api/user/signup")
    public String signup(@RequestBody SignupRequestDto requestDto,
                         @AuthenticationPrincipal String email) { // ✅ 핵심: 스프링 시큐리티가 토큰을 해석해서 여기에 이메일을 쏙 넣어줍니다!

        if (email != null) {
            userService.signup(email, requestDto.getNickname());
            return "success";
        }
        return "fail";
    }
}