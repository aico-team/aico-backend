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

    // 친구 요청 발신
    @PostMapping("/{nickName}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable String nickName,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        friendService.sendFriendRequest(nickName, userDetails);
        return ResponseEntity.ok("요청 발신 완료");
    }

    // 대기 중인 수신한 요청 조회
    @GetMapping
    public ResponseEntity<List<FriendDto>> getRequests(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<FriendDto> waitingRequests = friendService.getWaitingFriendRequests(userDetails);
        return ResponseEntity.ok(waitingRequests);
    }

    // 친구 요청 수락
    @PostMapping("/accept/{id}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String response = friendService.acceptWaitingRequest(id, userDetails);
        return ResponseEntity.ok(response);
    }

    // 친구 요청 거절 및 친구 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteOrReject(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String response = friendService.deleteOrReject(id, userDetails);
        return ResponseEntity.ok(response);
    }
}
