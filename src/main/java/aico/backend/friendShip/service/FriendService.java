package aico.backend.friendShip.service;


import aico.backend.friendShip.domain.FriendDto;
import aico.backend.friendShip.domain.FriendShip;
import aico.backend.friendShip.domain.FriendShipStatus;
import aico.backend.friendShip.repository.FriendShipRepository;
import aico.backend.global.exception.friendShip.AlreadyExistsException;
import aico.backend.global.exception.friendShip.FriendShipNotFoundException;
import aico.backend.global.exception.user.AccessDeniedException;
import aico.backend.global.exception.user.UserNotFoundException;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import aico.backend.user.repository.UserRepository;
import aico.backend.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FriendService {
    private final UserRepository userRepository;
    private final FriendShipRepository friendShipRepository;
    private final UserService userService;

    // 친구 요청 전송
    @Transactional
    public void sendFriendRequest(String toNickName, UserDetailsImpl userDetails) {
        // 발신 유저
        User fromUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException("User ID: " + userDetails.getId() + " not found"));
        String fromNickName = fromUser.getNickname();

        // 수신 유저
        User toUser = userRepository.findByNickname(toNickName)
                .orElseThrow(() -> new UserNotFoundException("User NickName: " + toNickName + " not found"));

        if (friendShipRepository.existsByUserAndFriendUserId(fromUser, toUser.getId())) {
            throw new AlreadyExistsException("이미 존재하는 친구 또는 요청입니다.");
        }

        // 발신 측에 저장
        FriendShip friendShipFrom = FriendShip.builder()
                .friendNickname(toNickName)
                .friendUserId(toUser.getId())
                .status(FriendShipStatus.WAITING)
                .isRequested(false)
                .build();

        // 수신 측에 저장
        FriendShip friendShipTo = FriendShip.builder()
                .friendNickname(fromNickName)
                .friendUserId(fromUser.getId())
                .status(FriendShipStatus.WAITING)
                .isRequested(true)
                .build();

        fromUser.addFriendShip(friendShipFrom);
        toUser.addFriendShip(friendShipTo);

        friendShipRepository.save(friendShipFrom);
        friendShipRepository.save(friendShipTo);

        friendShipTo.assignCounterId(friendShipFrom.getId());
        friendShipFrom.assignCounterId(friendShipTo.getId());

        log.info("전송 완료");
    }

    // 대기 중인 요청들 조회
    public List<FriendDto> getWaitingFriendRequests(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        List<FriendShip> waitingRequests = friendShipRepository.findWaitingRequest(user, FriendShipStatus.WAITING);
        List<FriendDto> friendDtos = new ArrayList<>();

        for (FriendShip friend : waitingRequests) {
            Long friendUserId = friend.getFriendUserId();
            String friendNickname = friend.getFriendNickname();
            friendDtos.add(new FriendDto(friend.getId(), friendUserId, friendNickname));
        }

        return friendDtos;
    }

    // 친구 요청 수락
    @Transactional
    public String acceptWaitingRequest(Long friendShipId, UserDetailsImpl userDetails) {
        FriendShip myFriendShip = friendShipRepository.findWithUserById(friendShipId)
                .orElseThrow(() -> new FriendShipNotFoundException("존재하지않는 요청입니다. ID: " + friendShipId));

        User me = myFriendShip.getUser();
        if (!me.equals(userDetails.getUser()) || !myFriendShip.isRequested()){
            throw new AccessDeniedException("수락 권한이 없습니다.");
        }

        FriendShip counterFriendShip = friendShipRepository.findById(myFriendShip.getCounterId())
                .orElseThrow(() -> new FriendShipNotFoundException("존재하지않는 요청입니다. ID: " + myFriendShip.getCounterId()));

        myFriendShip.acceptRequest();
        counterFriendShip.acceptRequest();

        return "수락 완료";
    }

    // 친구 삭제 및 요청 거절
    @Transactional
    public String deleteOrReject(Long friendShipId, UserDetailsImpl userDetails) {
        FriendShip myFriendShip = friendShipRepository.findById(friendShipId)
                .orElseThrow(() -> new FriendShipNotFoundException("친구 관계 ID: " + friendShipId + " 존재 하지않음"));

        if (!myFriendShip.getUser().equals(userDetails.getUser())){
            throw new AccessDeniedException("거절 권한이 없습니다.");
        }

        Long counterId = myFriendShip.getCounterId();

        friendShipRepository.deleteById(friendShipId);
        friendShipRepository.deleteById(counterId);

        return "거절 완료";
    }

}
