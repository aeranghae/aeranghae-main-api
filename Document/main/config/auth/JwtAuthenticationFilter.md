이 경비원은 건물로 들어오려는 모든 사람(요청)을 막아 세우고, 편지봉투(헤더)에 사원증(JWT)이 들어있는지 확인한 뒤, 아까 만든 '감별 기계(`JwtTokenProvider`)'에 넣고 돌려보는 아주 중요한 역할을 합니다.

---

### 👮‍♂️ 1. 경비원의 소속과 장비 (클래스 선언과 변수)



```Java
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
```

- **`extends OncePerRequestFilter`**: 아주 똑똑한 스프링의 기본 경비원 클래스를 상속받았습니다. 이름 그대로 **"한 번의 요청(Request) 당 딱 한 번(Once)만 검사해라!"**라는 뜻입니다. (안 그러면 방문자가 로비에서 엘리베이터 탈 때마다 똑같은 사원증 검사를 반복받는 불상사가 생깁니다.)
- **`jwtTokenProvider`**: 이 경비원에게 아까 우리가 만든 **'사원증 감별 기계'**를 쥐여줍니다. (`@RequiredArgsConstructor` 덕분에 스프링이 알아서 챙겨줍니다.)

---

### 🕵️‍♂️ 2. 경비원의 실제 근무 수칙 (`doFilterInternal` 메서드)

방문자가 문을 두드릴 때마다 이 메서드가 실행됩니다. 여기가 핵심입니다!

```Java
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. "손님, 편지봉투(헤더) 좀 보여주시죠."
        String token = resolveToken(request);
```

- 방문자가 들고 온 요청서(`request`)를 `resolveToken`이라는 헬퍼 메서드(아래에 설명)에 넘겨서, 봉투에 진짜 사원증이 들어있는지 뜯어봅니다.

```Java
        // 2. "사원증이 있네요? 감별 기계에 한 번 넣어보겠습니다."
        if (token != null && jwtTokenProvider.validateToken(token)) {
```

- 봉투가 비어있지 않고(`token != null`), 기계에 넣었을 때 띠링! 하고 진짜/유효 판정이 나면(`validateToken`) 안으로 들여보낼 준비를 합니다.

```Java
            // 3. "기계에 돌려보니 이메일이 OOO이시군요."
            String email = jwtTokenProvider.getUserEmail(token);
```

- 감별 기계의 돋보기 기능을 써서, 사원증 안에 적힌 이메일을 쏙 빼옵니다. ([[JwtTokenProvider]] 클래스에 있음)

```Java
            // 4. "본사 보안 시스템에 이분이 '정식 통과자'라고 쾅! 도장을 찍어두겠습니다."
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
```

- **여기가 스프링 시큐리티의 핵심 마법입니다.** * 경비원은 방금 알아낸 이메일을 `UsernamePasswordAuthenticationToken`이라는 스프링 전용 방문증에 옮겨 적습니다.
- 그리고 `SecurityContextHolder` (건물 전체에 공유되는 보안 시스템 게시판)에 이 방문증을 딱! 붙여둡니다.
- **이 도장을 찍어둬야**, 나중에 우리가 `UserApiController`에서 `@AuthenticationPrincipal String email`이라고 적었을 때 스프링이 "아, 아까 게시판에 붙어있던 그 이메일이구나!" 하고 컨트롤러에 쏙 넣어줄 수 있습니다.

```Java
        // 5. "검사 끝났습니다. 다음 문으로 들어가시죠."
        filterChain.doFilter(request, response);
    }
```

- 검사가 끝나면 다음 필터(혹은 최종 목적지인 컨트롤러)로 사람을 통과시킵니다.
    
- **💡 질문:** 만약 방문자가 사원증을 아예 안 들고 왔으면(`token == null`) 어떻게 될까요?
    - `if` 문을 타지 않고 그냥 `doFilter`로 통과됩니다! "어? 막아야 하는 거 아니에요?" 라고 생각하실 수 있습니다.
    - 하지만 걱정 마세요. 일단 '미인증 방문객' 신분으로 통과시킨 다음, 아까 `SecurityConfig`에서 설정해 둔 **"로비(구글 로그인 API) 빼고는 다 나가!"(`anyRequest().authenticated()`)라는 건물 규칙**이 벼락같이 떨어져서 쫓아내 버립니다. (역할 분담이 아주 기가 막히죠!)

---

### ✉️ 3. 편지봉투 뜯는 기술 (`resolveToken` 헬퍼 메서드)

경비원이 편지봉투(HTTP Header)에서 사원증만 쏙 빼내는 요령입니다.

```Java
    private String resolveToken(HttpServletRequest request) {
        // "Authorization 이라는 글자가 적힌 편지봉투를 찾아라!"
        String bearerToken = request.getHeader("Authorization");
        
        // "봉투가 있고, 그 안에 내용물이 'Bearer '라는 암호로 시작한다면..."
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "앞에 있는 'Bearer ' (7글자)는 떼어버리고, 뒤에 있는 진짜 플라스틱 사원증(토큰)만 꺼내라!"
            return bearerToken.substring(7);
        }
        return null; // 봉투가 없거나 암호가 틀리면 빈손으로 돌아감.
    }
```

- 전 세계 개발자들이 약속한 표준 규칙(RFC 6750)입니다. JWT 토큰을 보낼 때는 무조건 헤더 이름을 `Authorization`으로 하고, 값은 `Bearer 엄청나게긴토큰문자열` 형태로 보내기로 약속했거든요. 그래서 앞의 7글자(`Bearer` )를 가위로 싹둑 자르고 뒤의 알맹이만 꺼내는 것입니다.

---
