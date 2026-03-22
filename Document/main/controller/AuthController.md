아주 좋습니다! 철통같은 방어벽(Security)을 무사히 통과하거나, 아직 신분증이 없는 손님이 가장 먼저 찾아오는 곳 이 안내 데스크 직원은 머리 아픈 복잡한 일은 직접 하지 않습니다. 대신 뒤에 있는 실무 부서들에게 전화를 걸어 일을 착착 나눠주고, 결과물만 예쁘게 포장해서 손님(리액트)에게 돌려주는 **'접수 및 안내'** 역할을 기가 막히게 수행합니다.

---

### 🪧 1. 안내 데스크의 간판과 규칙 (어노테이션)

```Java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
```

- **`@RestController`**: 스프링에게 "이 데스크는 예쁜 화면(HTML) 같은 건 안 키웁니다. 오로지 **순수한 데이터(JSON)**만 뱉어내는 현대적인 API 창구입니다!" 라고 선언하는 간판입니다.
    
- **`@RequestMapping("/api/auth")`**: 이 안내 데스크의 주소(위치)입니다. 누군가 `/api/auth`로 시작하는 주소로 찾아오면 무조건 이 데스크로 안내됩니다.
    
- **`@RequiredArgsConstructor`**: 롬복(Lombok)의 마법입니다. 아래에 있는 `final` 직원들을 스프링이 알아서 이 데스크에 출근(주입)시켜 줍니다.
    
- **`@CrossOrigin(...)`**: "원래 외부인(다른 포트)은 출입 금지지만, `http://localhost:5173`에서 온 리액트 손님만큼은 특별히 문을 활짝 열어주어라!" 라는 VIP 통과 명단입니다. (앞서 SecurityConfig에서 설정한 것과 더불어 컨트롤러 단에서도 한 번 더 명시해 준 꼼꼼한 설정입니다.)
    

---

### 👨‍💼 2. 데스크에 배치된 직원과 기계 (변수)

안내 데스크 직원이 일을 처리하기 위해 양옆에 끼고 있는 핵심 조력자들입니다.

```Java
    private final GoogleAuthService googleAuthService;
    private final JwtTokenProvider jwtTokenProvider;
```

- **`googleAuthService` (인사팀 실무자)**: 구글 본사에 전화를 걸어 손님이 가져온 구글 신분증이 진짜인지 확인하고, 진짜면 우리 회사 DB에 회원가입까지 시켜주는 힘센 직원입니다.
    
- **`jwtTokenProvider` (사원증 발급 기계)**: 아까 우리가 해부했던 바로 그 기계입니다! 우리 건물 전용 '애랑해 VIP 출입증(JWT)'을 뚝딱 찍어냅니다.

---

### 🛎️ 3. 손님 응대 매뉴얼 (`googleLogin` 메서드)

리액트가 구글 로그인 팝업에서 받은 '구글 임시 신분증'을 들고 찾아왔을 때, 직원이 응대하는 정확한 순서도입니다.

```Java
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
```

- **`@PostMapping("/google")`**: "문서를 제출(POST)하러 온 손님 중, `/api/auth/google`로 온 손님은 내가 처리하겠다!"
    
- **`@RequestBody GoogleLoginRequest request`**: 리액트가 던진 JSON 편지봉투(`{ "credential": "어쩌구저쩌구..." }`)를 뜯어서, 자바가 읽기 편하게 `request`라는 바구니에 예쁘게 담아옵니다.


```Java
        try {
            // 1. "인사팀! 이 구글 신분증 좀 검사하고, DB에 등록해 주세요!"
            User user = googleAuthService.verifyTokenAndLogin(request.getCredential());
```

- 핵심 로직의 시작입니다. 편지봉투 안에 있던 구글 토큰(`getCredential`)을 꺼내서 인사팀(`googleAuthService`)에게 휙 던집니다.
    
- 인사팀이 알아서 뚝딱뚝딱 검증하고 가입시킨 뒤, **완성된 유저 정보(`User` 엔티티)**를 결과물로 돌려줍니다.


```Java
            // 2. "등록 끝났군요! 자, 이제 애랑해 전용 사원증을 찍어내자!"
            String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
```

- 방금 가입된(또는 로그인된) 유저의 이메일과 권한을 기계(`jwtTokenProvider`)에 넣고 버튼을 누릅니다.
    
- 그러면 위조 불가능한 긴 문자열, 즉 **우리만의 JWT 통행증(`accessToken`)**이 발급됩니다!

```Java
            // 3. "손님, 환영합니다! 여기 사원증이랑, 확인차 손님 프로필 정보 담아드릴게요."
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("picture", user.getPicture());

            return ResponseEntity.ok(response);
```

- 그냥 통행증만 툭 던져주면 프론트엔드(리액트)가 화면에 "OOO님 환영합니다!"를 띄우기 위해 API를 한 번 더 호출해야 하는 번거로움이 생깁니다.
- 그래서 센스 있는 데스크 직원은 **빈 상자(`HashMap`)를 하나 가져와서, 방금 만든 통행증과 유저의 이름, 이메일, 프사 URL을 예쁘게 포장**합니다.
- 그리고 `ResponseEntity.ok()`를 통해 **"200 OK (성공) 도장"**을 쾅 찍어서 리액트로 택배를 보냅니다!

```Java
        } catch (Exception e) {
            // "구글 신분증이 가짜거나 만료됐네요! 당장 나가세요!"
            return ResponseEntity.status(401).body("로그인 실패: " + e.getMessage());
        }
    }
```

- 만약 1번 단계에서 인사팀이 "이거 가짜 토큰인데요?!" 하고 화를 내면(Exception 발생), 즉시 밑으로 빠져나옵니다.
    
- 그리고 리액트에게 **"401 Unauthorized (인증 실패)"** 에러 딱지와 함께 왜 실패했는지 이유를 적어서 돌려보냅니다.
    

---
