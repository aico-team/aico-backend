package aico.backend.friendShip.controller;

import aico.backend.friendShip.domain.FriendDto;
import aico.backend.friendShip.service.FriendService;
import aico.backend.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/{nickName}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable String nickName,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        friendService.sendFriendRequest(nickName, userDetails);
        return ResponseEntity.ok("요청 발신 완료");
    }

}
