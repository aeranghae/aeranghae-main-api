
단지 제출하는 곳과 목적이 다를 뿐이죠. 아까는 1층 로비에 내는 '로그인 신청서'였다면, 이번에는 사내 민원 창구(`UserApiController`)에 제출하는 **'닉네임 변경 신청서'**입니다.

---

### 📝 '닉네임 변경 신청서' 봉투 해부

```Java
@Getter
@NoArgsConstructor
public class SignupRequestDto {
```

- **`@Getter` (내용물 확인용 지퍼)**
    
    - 창구 직원(`UserApiController`)이 이 봉투를 받았을 때, 안에 적힌 닉네임을 꺼내 볼 수 있게 해줍니다.
        
    - 덕분에 아까 컨트롤러 코드에서 `requestDto.getNickname()` 이라고 편하게 꺼내 쓸 수 있었죠!
        
- **`@NoArgsConstructor` (빈 서류 양식 준비)**
    
    - 스프링의 택배 분류기(Jackson)가 리액트에서 날아온 JSON 데이터를 자바로 바꿀 때, **"일단 빈 닉네임 변경 신청서를 하나 출력해라!"** 라고 명령하는 부분입니다. 빈 양식이 있어야 거기에 글씨를 옮겨 적을 수 있으니까요.
        

```Java
    private String nickname;
}
```

- **`nickname` (작성란)**
    
    - 리액트에서 사용자가 입력한 닉네임이 담기는 정확한 칸입니다.
        
    - 여기서도 똑같이 **리액트에서 보내는 JSON 이름과 완벽하게 일치해야** 합니다. 우리가 리액트 `App.tsx`에서 `axios.post(..., { nickname: nicknameInput })` 라고 보냈기 때문에, 스프링이 이 칸에 찰떡같이 닉네임을 꽂아 넣어준 것입니다.
        

---