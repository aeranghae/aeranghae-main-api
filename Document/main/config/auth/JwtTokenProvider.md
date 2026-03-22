
이 클래스는 딱 두 가지 역할을 합니다. **1) 새 직원에게 사원증을 찍어주는 역할**, **2) 들어오는 직원의 사원증이 진짜인지 돋보기로 들여다보는 역할**입니다. 아주 흥미로운 암호화 기술이 들어가 있으니 위에서부터 차근차근 해부해 보죠!

---

### 🏭 1. 기계 등록과 재료 준비 (어노테이션 & 변수)



```Java
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;
```

- **`@Component`**: 스프링에게 "이 기계를 스프링 공장(Application Context)에 하나 배치해 둬! 나중에 다른 부서(컨트롤러나 필터)에서 이 기계를 가져다 쓸 거야!"라고 선언하는 마크입니다.
- **`secretKey`**: 이 기계의 핵심, **'애랑해 본사 특수 인감도장'**입니다. 이 도장이 찍힌 출입증만 진짜로 인정받습니다.
- **`validityInMilliseconds`**: 출입증의 유효기간입니다. 밀리초 단위로 계산됩니다.

---

### ⚙️ 2. 기계 초기 세팅 (생성자)

스프링이 켜질 때 이 기계가 조립되면서 인감도장을 파는 과정입니다.



```Java
    public JwtTokenProvider(
            @Value("${jwt.secret:vmfhaltmskdlstkfkdgodyauddlsclfrn1234567890!@#}") String secret,
            @Value("${jwt.expiration:3600000}") long validityInMilliseconds) { // 기본 1시간
            
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }
```

- **`@Value("${...:기본값}")`**: `application.yml` 파일에서 `jwt.secret`이라는 변수를 찾아옵니다. 만약 못 찾으면 뒤에 적어둔 아주 긴 텍스트(기본값)를 가져옵니다.
- **`Keys.hmacShaKeyFor(...)`**: 평범한 문자열(비밀번호)을 그대로 도장으로 쓰면 보안에 취약합니다. 그래서 이 메서드를 써서 일반 글자를 해커가 절대 풀 수 없는 **강력한 암호화 알고리즘(HMAC-SHA)이 적용된 진짜 인감도장(`SecretKey`)으로 깎아냅니다.**

---

### 🖨️ 3. 출입증 찍어내기 (`createToken` 메서드)

구글 로그인을 갓 마친 신규 유저에게 애랑해 전용 통행증을 발급해 주는 과정입니다.
```Java
    public String createToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds); // 지금 시간 + 1시간

        return Jwts.builder()
                .subject(email) // 1. 카드 표면에 주인의 '이메일' 적기
                .claim("role", role) // 2. 뒷면에 '권한(USER/ADMIN)' 적어두기
                .issuedAt(now) // 3. 발급일시 도장 찍기
                .expiration(validity) // 4. 만료일시 도장 찍기
                .signWith(secretKey) // 🌟 5. 가장 중요! 위조 방지용 '특수 인감도장' 쾅 찍기
                .compact(); // 6. 플라스틱 카드를 문자열 형태로 코팅해서 압축!
    }
```

- 해커가 이 카드를 주워서 본인의 이메일로 살짝 바꾸려고 시도할 수 있습니다. 하지만 카드를 수정하는 순간 **마지막에 찍어둔 `secretKey` 도장이 깨져버리기 때문에** 위조가 절대 불가능해집니다.

---

### 🔍 4. 출입증 읽어내기 (`getUserEmail` 메서드)

직원이 출입증을 내밀었을 때, 그 안에 적힌 이메일을 쏙 빼오는 돋보기 기능입니다.

```Java
    public String getUserEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // 1. 인감도장 대조용 돋보기 장착
                .build() // 2. 돋보기 완성
                .parseSignedClaims(token) // 3. 출입증(문자열)을 분해해서 내용물 확인!
                .getPayload() // 4. 카드에 적힌 정보들(Payload) 뭉치 꺼내기
                .getSubject(); // 5. 그중에서 주인의 이름(Subject = 이메일)만 쏙 빼오기
    }
```

---

### 🛡️ 5. 위조 및 만료 감별하기 (`validateToken` 메서드)

이 출입증이 1) 내가 발급한 게 맞는지, 2) 누군가 조작하지 않았는지, 3) 유효기간(1시간)이 지나지 않았는지 철저하게 검사하는 감별기입니다.

```Java
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true; // 에러 없이 여기까지 왔다면 완벽한 진짜 출입증!
        } catch (Exception e) {
            // 조작된 토큰이거나, 만료 시간이 1초라도 지났으면 여기서 에러(Exception)가 터집니다!
            return false; // "가짜거나 만료됐습니다! 쫓아내세요!"
        }
    }
```

- 자바의 `try-catch` 문을 아주 영리하게 사용한 부분입니다. `jjwt` 라이브러리는 도장이 다르거나 시간이 만료된 토큰을 분해(`parseSignedClaims`)하려고 하면 자비 없이 곧바로 에러를 던져버립니다. 에러가 나면 조용히 `catch`로 넘어가서 `false`를 리턴해 입구를 막아버리는 원리입니다.

---

기계가 어떻게 도장을 파고, 카드를 찍어내고, 다시 검사하는지 직관적으로 머릿속에 그려지시나요? 이 클래스는 거의 모든 스프링 + JWT 프로젝트에서 표준처럼 쓰이는 구조라 한 번 이해해 두면 아주 든든한 무기가 됩니다.