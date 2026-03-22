이 부서는 프론트엔드가 던져준 '구글 임시 신분증'을 들고 **구글 본사에 직접 전화를 걸어 위조 여부를 확인**하고, 진짜로 판명되면 **사내 직원 명부(DB)를 최신화**하는 아주 막중한 임무를 띠고 있습니다.

---

### 🏢 1. 인사팀 사무실 세팅 (어노테이션 & 변수)



```Java
@RequiredArgsConstructor
@Service
public class GoogleAuthService {

    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
```

- **`@Service`**: 스프링에게 "여기는 비즈니스 로직(실제 업무)을 처리하는 실무 부서입니다!"라고 알리는 명패입니다. 이 명패가 있어야 컨트롤러가 이 부서를 호출할 수 있습니다.
    
- **`userRepository` (사내 직원 명부)**: DB에 접근해서 유저를 찾고, 저장하고, 수정할 수 있는 마법의 명부입니다.
    
- **`googleClientId` (애랑해 고유 사업자 번호)**: 구글 콘솔에서 발급받았던 그 긴 ID입니다. 구글 본사에 "우리 애랑해 서비스인데, 이 신분증 확인 좀 해줘!"라고 요청할 때 우리 신분을 밝히기 위해 `application.yml`에서 가져옵니다.
    

---

### 💼 2. 본격적인 신원조회 업무 시작 (`verifyTokenAndLogin` 메서드)

안내 데스크에서 넘겨준 구글 토큰(`credential`)을 들고 본격적인 검증에 들어가는 메서드입니다.

```Java
    @Transactional
    public User verifyTokenAndLogin(String credential) throws Exception {
```

- **`@Transactional` (🌟 아주 중요!)**: 이 업무를 수행하다가 중간에 에러가 나서 뻗어버리면, 여태까지 DB에 건드렸던 내용들을 **"없던 일로(Rollback)"** 싹 되돌려주는 생명줄입니다. DB를 수정하거나 저장하는 서비스 로직에는 무조건 붙여주는 것이 원칙입니다.

#### 📞 Step 1 & 2: 구글 본사에 위조 감별 요청하기

```Java
        // 1. 구글 토큰 검증기 생성 (구글 본사와 통신할 전용 전화기 개통)
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId)) // "이 토큰이 우리 애랑해(googleClientId)를 위해 발급된 게 맞는지 확인해 줘!"
                .build();

        // 2. 토큰이 진짜인지 구글 서버를 통해 확인
        GoogleIdToken idToken = verifier.verify(credential);

        if (idToken == null) {
            throw new IllegalArgumentException("유효하지 않은 구글 토큰입니다.");
        }
```

- **`verifier.verify(...)`**: 구글이 공식적으로 제공하는 라이브러리를 사용해, 프론트엔드가 가져온 토큰이 조작되지 않았는지, 만료되지 않았는지, 그리고 다른 앱이 아니라 **정확히 우리 '애랑해'를 위해 발급된 토큰이 맞는지** 깐깐하게 3중 검사합니다.
    
- 가짜거나 만료된 토큰이면 `null`을 뱉어내고, 즉시 "유효하지 않은 토큰입니다!" 하고 에러를 던지며 업무를 중단합니다.

#### 🪪 Step 3: 신분증 내용물(Payload) 확인하기

```Java
        // 3. 진짜라면 토큰 안에서 사용자 정보(페이로드) 꺼내기
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
```

- 구글이 "어, 그거 내가 발급해 준 진짜 맞아!"라고 도장을 찍어주면(`idToken != null`), 그제야 안심하고 신분증 포장을 뜯습니다(`getPayload()`).
    
- 그 안에서 회원가입/로그인에 필요한 핵심 정보인 **이메일, 이름, 프로필 사진 URL**만 쏙쏙 뽑아냅니다.
    

#### 📝 Step 4 & 5: 직원 명부(DB) 최신화 및 저장

가장 핵심적인 로직이자, 자바 8의 `Optional`과 람다(Lambda) 식을 아주 우아하게 사용한 부분입니다!

```Java
        // 4. 우리 DB에 있는 회원인지 확인 후, 없으면 회원가입, 있으면 정보 업데이트
        User user = userRepository.findByEmail(email)
                .map(entity -> entity.update(name, pictureUrl)) // 기존 회원이면 이름/프사 업데이트
                .orElse(User.builder()
                        .email(email)
                        .name(name)
                        .picture(pictureUrl)
                        .role(Role.USER) // 혹은 기본 권한 설정
                        .build()); // 신규 회원이면 객체 생성
```

- **`findByEmail(email)`**: 사내 직원 명부(`UserRepository`)를 촤르륵 넘기며 방금 구글에서 가져온 이메일이 있는지 찾습니다.
    
- **`.map(...)`**: **(이미 명부에 이름이 있다면)** 유저가 구글에서 프사나 닉네임을 바꿨을 수도 있으니, 최신 정보로 쓱쓱 업데이트해 줍니다.
    
- **`.orElse(...)`**: **(명부를 다 뒤져도 없다면 = 처음 온 뉴비라면)** 새로운 근로 계약서(`User.builder()`)를 꺼내서 이메일, 이름, 사진을 적고 기본 권한(`Role.USER`)을 부여해서 새 직원을 생성합니다.
    
```Java
        return userRepository.save(user); // DB에 저장
    }
```

- 기존 직원이든 새로 만든 직원이든, **최종적으로 `save()`를 호출해서 DB에 도장을 쾅! 찍고** 그 완성된 유저 정보를 안내 데스크(`AuthController`)로 다시 넘겨줍니다.
    
- 그러면 아까 우리가 봤던 것처럼, 안내 데스크 직원이 이 정보를 바탕으로 '애랑해 자체 JWT'를 발급하게 되는 것이죠!
    

---
