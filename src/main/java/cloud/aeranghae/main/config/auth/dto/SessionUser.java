package cloud.aeranghae.main.config.auth.dto;

import cloud.aeranghae.main.domain.User;
import lombok.Getter;
import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;
    private String role;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
        this.role = user.getRoleKey(); // 추가된 부분 (예: ROLE_GUEST)
    }
}