package aico.backend.friendShip.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class FriendDto {
    private Long friendShipId;
    private Long friendUserId;
    private String friendNickName;

    public FriendDto(Long friendShipId, Long userId, String nickName) {
        this.friendShipId = friendShipId;
        this.friendUserId = userId;
        this.friendNickName = nickName;
    }
}
