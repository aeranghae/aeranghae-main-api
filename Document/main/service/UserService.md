
---

### 👨‍🔧 1. 실무자의 소속과 도구 (어노테이션 & 변수)

```Java
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
```

- **`@Service`**: 스프링에게 "저는 단순한 안내 데스크(Controller)가 아니라, **실제로 땀 흘리며 비즈니스 로직을 처리하는 실무자**입니다!" 라고 신고하는 명찰입니다.
    
- **`@RequiredArgsConstructor` & `userRepository`**: 실무자가 일할 때 반드시 필요한 **'애랑해 직원 명부(DB 조종기)'**를 스프링이 알아서 챙겨줍니다.
    

---

### ✍️ 2. 핵심 업무 처리 (`signup` 메서드)

민원 창구에서 넘겨준 '이메일'과 '새로운 닉네임'을 들고 본격적인 업무를 시작합니다.

```Java
    @Transactional
    public void signup(String email, String nickname) {
```

- **`@Transactional` (🌟 생명줄)**: 실무자가 서고에 들어가서 작업할 때 거는 **'완벽주의 마법'**입니다.
    
- "이 업무를 하다가 중간에 한 글자라도 삑사리가 나면(에러 발생), 여태 적었던 걸 싹 다 지우고 **원래 상태로 완벽하게 되돌려놔라(Rollback)!**" 라는 뜻입니다. 데이터가 꼬이는 걸 막아주는 아주 든든한 방패죠.
    

#### 🗂️ Step 1: 서고에서 서류 찾기

```Java
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
```

- 실무자는 직원 명부(`userRepository`)를 뒤져서 창구에서 넘겨준 `email`과 똑같은 사람의 서류(`User`)를 꺼내옵니다.
    
- **`.orElseThrow(...)`**: 만약 명부를 아무리 뒤져도 그 이메일이 없다면? 당황하지 않고 즉시 업무를 중단한 뒤 "이런 사람은 없는데요?!" 하고 예외(에러)를 던져버립니다.
    

#### 🪄 Step 2: 서류 수정하기 (그리고 숨겨진 마법)

```Java
        // 엔티티의 승급 메서드 호출 (JPA 더티 체킹으로 자동 DB 업데이트)
        user.authorizeUser(nickname);
    }
}
```

- 찾아온 서류(`user`) 안에 있는 `authorizeUser`라는 기능(메서드)을 실행시켜서 닉네임을 쓱쓱 고쳐 적습니다. (아마 이 메서드 안에서 권한을 `GUEST`에서 `USER`로 올려주는 작업도 같이 하실 것 같네요!)
    

---

### 🚨 잠깐, 여기서 이상한 점 못 느끼셨나요?

과거에 DB를 다루던 방식이라면 서류를 수정하고 나서 반드시 이런 코드가 있어야 합니다.

`userRepository.save(user); // "수정한 서류를 다시 DB에 저장해 줘!"`

그런데 우리 코드에는 **저장(`save`)하는 코드가 없습니다!** 그냥 `user.authorizeUser(nickname);` 하고 쿨하게 메서드가 끝나버리죠. 그래도 DB에는 닉네임이 완벽하게 저장됩니다. 도대체 왜 그럴까요?

이게 바로 주석에 적혀있는 **'JPA 더티 체킹(Dirty Checking, 상태 변경 검사)'** 이라는 엄청난 마법 때문입니다.

1. **관찰 카메라 작동**: 아까 메서드 위에 `@Transactional`을 붙이고 서고 문을 열었죠? 이때부터 스프링(JPA)은 실무자가 꺼낸 서류(`user`)에 관찰 카메라를 달아둡니다.
    
2. **변경 감지**: 실무자가 `authorizeUser`를 호출해서 닉네임을 지우고 새 닉네임을 적습니다. (서류가 '더티(수정됨)' 상태가 되었습니다.)
    
3. **자동 저장 (마법의 순간)**: 실무자가 일을 마치고 서고 문을 닫고 나가는 순간(메서드가 끝나는 순간), 스프링이 관찰 카메라를 슥 돌려봅니다.
    
4. **"어? 들어올 때랑 비교해 보니까 닉네임이 바뀌었네? 내가 알아서 DB에 UPDATE 쿼리 날려서 저장해 줄게!"**
    

이 마법 덕분에 우리는 일일이 `save()`를 호출할 필요 없이, 그저 객체(엔티티)의 값을 바꾸기만 하면 알아서 데이터베이스가 업데이트되는 우아한 객체지향 프로그래밍을 할 수 있게 된 것입니다!

---
