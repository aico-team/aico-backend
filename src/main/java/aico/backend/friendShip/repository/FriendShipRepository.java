package aico.backend.friendShip.repository;

import aico.backend.friendShip.domain.FriendShip;
import aico.backend.friendShip.domain.FriendShipStatus;
import aico.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendShipRepository extends JpaRepository<FriendShip, Long> {

    @Query("""
        SELECT f FROM FriendShip f
        WHERE f.user = :user
        AND f.isRequested = true
        AND f.status = :status
""")
    List<FriendShip> findWaitingRequest(@Param("user") User user, @Param("status")FriendShipStatus status);
    boolean existsByUserAndFriendUserId(User user, Long friendUserId);
    Optional<FriendShip> findByFriendUserId(Long friendUserId);
    void deleteByFriendUserId(Long friendUserId);
}
