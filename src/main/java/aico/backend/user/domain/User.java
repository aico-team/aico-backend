package aico.backend.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private User(String nickname, String email, String password, Role role) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User createUser(String nickname, String email, String password, Role role) {
        return new User(nickname, email, password, role);
    }

}
