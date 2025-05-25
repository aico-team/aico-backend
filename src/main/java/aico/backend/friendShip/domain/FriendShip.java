package aico.backend.friendShip.domain;

import aico.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(schema = "aico")
public class FriendShip {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String friendNickname;
    private Long friendUserId;
    private Long counterId;

    private boolean isRequested;

    @Enumerated(value = EnumType.ORDINAL)
    private FriendShipStatus status;

    @Builder
    public FriendShip(String friendNickname, Long friendUserId, FriendShipStatus status, boolean isRequested) {
        this.friendNickname = friendNickname;
        this.friendUserId = friendUserId;
        this.status = status;
        this.isRequested = isRequested;
    }

    public void acceptRequest() {
        this.status = FriendShipStatus.ACCEPTED;
    }

    public void assignCounterId(Long counterId) {
        this.counterId = counterId;
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
