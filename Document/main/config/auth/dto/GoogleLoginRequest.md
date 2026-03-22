
---

### ✉️ 규격 편지봉투의 구조 해부



```Java
@Getter 
@NoArgsConstructor
public class GoogleLoginRequest {
```

- **`@Getter` (지퍼 달기)**
    
    - 봉투 안에 든 내용물(`credential`)을 꺼내 볼 수 있게 해주는 마법입니다.
        
    - 아까 `AuthController`에서 안내 데스크 직원이 `request.getCredential()`이라고 적어서 구글 토큰을 꺼냈던 것 기억하시나요? 바로 이 `@Getter`가 뒤에서 그 메서드를 몰래 만들어준 덕분입니다.
        
- **`@NoArgsConstructor` (빈 봉투 준비하기)**
    
    - **이게 제일 중요합니다!** 스프링(정확히는 Jackson이라는 번역기)은 리액트가 보낸 JSON 데이터(`{ "credential": "..." }`)를 자바로 변환할 때, **일단 '아무것도 안 든 텅 빈 봉투(기본 생성자)'를 먼저 하나 뚝딱 만든 다음**, 그 안에 내용물을 쏙쏙 채워 넣는 방식을 씁니다.
        
    - 만약 이 어노테이션이 없으면 스프링이 "어? 빈 봉투를 어떻게 접는지 모르겠는데?" 하면서 에러를 뱉어버립니다.
        



```Java
    // 리액트에서 보낼 토큰(credential)을 담을 변수
    private String credential;
}
```

- **`credential` (신분증 담는 칸)**
    
    - 실제 구글이 발급한 엄청나게 긴 암호문(토큰)이 담기는 칸입니다.
        
    - 여기서 가장 중요한 약속은 **"리액트가 보내는 JSON의 키(Key) 이름과 이 변수명이 토씨 하나 안 틀리고 똑같아야 한다"**는 것입니다.
        
    - 우리가 리액트 쪽 `axios.post` 코드에서 `{ credential: googleToken }` 이라고 적어서 보냈기 때문에, 스프링이 철자를 알아보고 이 칸에 정확히 쏙 넣어줄 수 있는 것입니다.
        

---