이 `SecurityConfig` 클래스는 애랑해(aeranghae) 백엔드 서버의 **'건물 전체 보안 설계도'**입니다. 위에서부터 아래로, 구역별로 나누어 아주 상세하게 뜯어보겠습니다.

---

### 🏛️ 1. 클래스 머리말 (어노테이션)

```Java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
```

- **`@Configuration`**: 스프링에게 "이 클래스는 설정 파일이야! 서버 켜질 때 제일 먼저 읽어서 세팅해 줘!"라고 알려주는 표지판입니다.
- **`@EnableWebSecurity`**: 스프링 시큐리티(보안망) 스위치를 'ON'으로 켭니다. 이 선언이 있어야 우리가 커스텀하는 보안 규칙들이 실제로 작동합니다.
- **`@RequiredArgsConstructor`**: 롬복(Lombok) 기능으로, 아래에 있는 `final` 변수들(`JwtTokenProvider`)을 스프링이 알아서 주입(연결)해주도록 만듭니다.
    

---

### 🎒 2. 준비물 (변수)

```Java
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;
```

- **`jwtTokenProvider`**: 아까 만든 출입증(JWT) 발급/검증 기계입니다. 나중에 필터를 낄 때 이 기계를 경비원에게 쥐여주기 위해 미리 준비해 둡니다.
- **`allowedOrigins`**: `application.yml`에서 설정한 허용 주소(`http://localhost:5173` 등)를 가져와서 리스트(List) 형태로 담아둡니다.

---

### 📜 3. 핵심 보안 규칙 (`filterChain` 메서드)

스프링 시큐리티는 수십 개의 '필터(거름망)'가 체인처럼 연결된 구조입니다. 이 메서드는 **"어떤 거름망은 빼고, 어떤 거름망은 새로 낄지"** 결정하는 가장 중요한 곳입니다.

```Java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

- **CORS (Cross-Origin Resource Sharing)**: 항구의 입항 허가서입니다. 리액트(5173 포트)와 스프링(8080 포트)은 주소가 다르기 때문에 브라우저가 기본적으로 통신을 막습니다. 아래에서 세부 설정한 `corsConfigurationSource` 규칙을 적용해서, 애랑해 리액트 앱의 접근을 허락해 줍니다.

```Java
                // 2. 화면 관련 보안 기능 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
```

- **`csrf` (Cross-Site Request Forgery)**: 기존 세션 기반의 웹사이트에서 해킹을 막기 위한 방어막입니다. 우리는 세션 대신 JWT를 쓰기 때문에 필요 없어서 끕니다(`disable`).
- **`formLogin` & `httpBasic`**: 스프링이 기본으로 제공하는 못생긴 기본 로그인 화면과 기본 인증 방식을 끕니다. 우리는 구글 팝업을 통해 우리만의 방식으로 로그인하니까요.

```Java
                // 3. Stateless(무상태) 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
```

- **`STATELESS`**: 가장 중요한 설정입니다! 서버에게 "이제부터 방문자(유저)를 메모리에 기억하지 마!"라고 명령합니다. 즉, 로그인한 유저의 정보를 세션에 저장하지 않고, 매 요청마다 날아오는 JWT만 보고 판단하겠다는 선언입니다.

```Java
                // 4. API 엔드포인트 권한(문지기) 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/google").permitAll()
                        .anyRequest().authenticated()
                )
```

- **`requestMatchers(...).permitAll()`**: `/api/auth/google` 주소는 아직 신분증(JWT)이 없는 사람들이 구글 토큰을 들고 오는 1층 로비이므로 무조건 출입을 허용(`permitAll`)합니다.
- **`.anyRequest().authenticated()`**: 그 외의 모든 API 요청은 무조건 인증된(신분증을 낸) 사람만 통과(`authenticated`)시키라는 뜻입니다.

```Java
                // 5. JWT 필터 끼워넣기!
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
```

- **`addFilterBefore`**: 스프링의 기본 신분 확인 경비원(`UsernamePasswordAuthenticationFilter`)이 일하기 **'전(Before)'**에, 우리가 직접 고용한 사설 경비원(`JwtAuthenticationFilter`)을 맨 앞에 세워두는 것입니다.
- "기본 경비원아, 넌 나서지 마. 우리 애랑해 건물은 무조건 내가 만든 JWT 필터가 먼저 검사할 거야!"라는 의미입니다.

---

### 🚪 4. CORS 세부 설정 (`corsConfigurationSource` 메서드)

아까 위에서 켰던 CORS(입항 허가)의 구체적인 규칙을 적어둔 문서입니다.

```Java
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins); // 허용할 주소 (리액트 도메인)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 행동 (조회, 생성, 수정, 삭제)
        config.setAllowedHeaders(List.of("*")); // 허용할 편지봉투의 헤더 (모두 허용)
        config.setAllowCredentials(true); // "인증 정보(토큰)를 실어 보내는 것도 허락할게!"

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 이 규칙을 우리 서버의 모든 API 주소("/**")에 적용!
        return source;
    }
```

---