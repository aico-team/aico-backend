package aico.backend.chat;

import aico.backend.user.domain.User;
import jakarta.persistence.*;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String request;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateRequest(String request) {
        this.request = request;
    }

    public void updateResponse(String response) {
        this.response = response;
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
