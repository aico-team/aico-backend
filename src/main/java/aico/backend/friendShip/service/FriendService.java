package aico.backend.friendShip.service;


import aico.backend.friendShip.domain.FriendDto;
import aico.backend.friendShip.domain.FriendShip;
import aico.backend.friendShip.domain.FriendShipStatus;
import aico.backend.friendShip.repository.FriendShipRepository;
import aico.backend.global.exception.friendShip.AlreadyExistsException;
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
        User fromUser = userService.getCurrentUser(userDetails).
                orElseThrow(() -> new UserNotFoundException(userDetails.getNickname() + " 이/가 존재하지 않습니다."));
        String fromNickName = fromUser.getNickname();

        // 수신 유저
        User toUser = userRepository.findByNickname(toNickName)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자가 없습니다."));

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
    public String acceptWaitingRequest(Long friendShipId) {
        FriendShip myFriendShip = friendShipRepository.findById(friendShipId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지않는 요청입니다."));

        FriendShip counterFriendShip = friendShipRepository.findById(myFriendShip.getCounterId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지않는 요청입니다."));

        myFriendShip.acceptRequest();
        counterFriendShip.acceptRequest();

        return "수락 완료";
    }

    @Transactional
    public String rejectWaitingRequest(Long friendShipId) {
        FriendShip myFriendShip = friendShipRepository.findById(friendShipId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지않는 요청입니다."));

        Long friendUserId = myFriendShip.getFriendUserId();

        friendShipRepository.deleteById(friendShipId);
        friendShipRepository.deleteByFriendUserId(friendUserId);

        return "거절 완료";
    }

}
