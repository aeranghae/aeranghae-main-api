
이 `UserApiController` 클래스는 애랑해 건물(서버) 1층 로비의 깐깐한 보안 검색대(`JwtAuthenticationFilter`)를 무사히 통과한 **정식 직원(로그인 유저)들이 찾아오는 '사내 민원 창구'**입니다.

---

### 🏢 1. 민원 창구의 간판과 일꾼 (어노테이션 & 변수)

```Java
@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;
```

- **`@RestController`**: "이 창구는 종이 서류(HTML 화면)는 안 받고, 오직 깔끔한 전산 데이터(JSON, 문자열)만 주고받습니다!"라는 뜻입니다.
    
- **`@RequiredArgsConstructor`**: 롬복(Lombok)이 이 창구에 꼭 필요한 일꾼(`userService`)을 자동으로 배치해 줍니다.
    
- **`userService`**: 창구 직원이 민원을 접수받으면, 실제로 서고(DB)에 내려가서 직원 명부를 수정할 **'진짜 실무자'**입니다.
    

---

### 📝 2. 닉네임 변경(회원가입 완료) 전용 창구 (`signup` 메서드)

리액트에서 "저 로그인 방금 했는데, 닉네임 이걸로 쓸래요!" 라고 요청을 보냈을 때 정확히 이 메서드가 실행됩니다.

```Java
    @PostMapping("/api/user/signup")
    public String signup(
```

- **`@PostMapping`**: "`/api/user/signup` 주소로 데이터를 들고(POST) 찾아온 손님은 내가 상대하겠다!"

#### 📦 Step 1: 손님이 들고 온 서류 봉투 열기

```Java
            @RequestBody SignupRequestDto requestDto,
```

- 리액트가 보낸 JSON 데이터(`{ "nickname": "애랑해짱" }`)를 받아서, 스프링이 미리 만들어둔 `SignupRequestDto`라는 자바 바구니에 예쁘게 담아줍니다. 이제 `requestDto.getNickname()`을 하면 '애랑해짱'이라는 글자를 꺼낼 수 있습니다.

#### 🪄 Step 2: (핵심!) 보안 검색대에서 넘겨준 신분 확인서 확인하기

```Java
            @AuthenticationPrincipal String email) {
```

- **이 코드가 바로 토큰(JWT) 방식의 꽃입니다!** 🌸
    
- 아까 우리가 `JwtAuthenticationFilter` (경비원) 클래스를 해부할 때 기억나시나요?
    
    경비원이 손님의 토큰을 검사한 뒤, 진짜면 이메일을 꺼내서 **건물 전체 게시판(`SecurityContextHolder`)에 "이 사람 이메일은 OOO입니다!" 하고 도장을 쾅 찍어뒀었죠.**
    
- **`@AuthenticationPrincipal`**은 바로 그 게시판을 쓱 쳐다보고, 경비원이 적어둔 그 '이메일'을 이 변수(`email`) 안에 마법처럼 쏙 넣어주는 역할을 합니다!
    
- 덕분에 이 창구 직원은 "손님 누구세요? 토큰 다시 줘보세요" 할 필요 없이, 이미 인증된 손님의 이메일을 바로 꽁으로 얻어다 쓸 수 있습니다.
    

#### 🛠️ Step 3: 실무자에게 일 시키고 결과 알려주기



```Java
        if (email != null) {
            // "UserService 실무자님! 이 이메일(email) 가지신 분 닉네임 이걸로 바꿔주세요!"
            userService.signup(email, requestDto.getNickname());
            return "success"; // 리액트에게 "처리 완료!" 문자 보내기
        }
        return "fail"; // 만약 게시판에 이메일이 없으면 실패 처리
    }
}
```

- 게시판에서 무사히 이메일을 가져왔다면(`email != null`), 진짜 일꾼인 `UserService`에게 이메일과 새 닉네임을 넘겨주며 DB를 수정하라고 지시합니다.
    
- 실무자가 일을 무사히 마치면, 창구 직원은 손님(리액트)에게 `"success"`라는 문자를 보내서 화면에 "닉네임 설정이 완료되었습니다!" 라는 알림이 뜰 수 있게 해줍니다.
    

---

### 💡 세션(Session) 방식과 비교해 볼까요?

과거 세션을 쓰던 시절에는 이 코드가 이랬습니다.

`SessionUser user = (SessionUser) httpSession.getAttribute("user");`

이 방식은 서버가 메모리(세션)라는 무거운 수첩을 낑낑대며 들고 다니면서 "어디 보자... 이 손님이 아까 누구였더라..." 하고 찾아보는 방식이었습니다.

하지만 지금 우리가 만든 토큰 방식은, **손님이 올 때마다 보여주는 사원증(JWT 토큰)을 경비원(Filter)이 딱 확인하고 이름표를 달아주면, 창구 직원(@AuthenticationPrincipal)은 그 이름표만 보고 바로 일을 처리하는**아주 빠르고 세련된 '무상태(Stateless)' 방식으로 진화한 것입니다!

이제 컨트롤러가 어떻게 보안 필터와 기가 막힌 협업을 하는지 완벽하게 이해되셨나요? 백엔드의 퍼즐이 정말로 다 맞춰졌습니다! 🧩