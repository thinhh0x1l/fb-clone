package com.fb.friend.repository;

import com.fb.friend.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bạn bè
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * Tìm mối quan hệ bạn bè giữa 2 người
     */
    @Query("SELECT f FROM Friend f WHERE ((f.requester.id = :userId1 AND f.addressee.id = :userId2) OR (f.requester.id = :userId2 AND f.addressee.id = :userId1)) AND f.deletedAt IS NULL")
    Optional<Friend> findFriendshipBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Lấy danh sách bạn bè của người dùng
     */
    @Query("SELECT f FROM Friend f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL")
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);

    /**
     * Lấy yêu cầu kết bạn đang chờ xử lý (người nhận)
     */
    @Query("SELECT f FROM Friend f JOIN FETCH f.requester WHERE f.addressee.id = :userId AND f.status = 'PENDING' AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<Friend> findPendingRequestsForUser(@Param("userId") Long userId);

    /**
     * Lấy yêu cầu kết bạn đã gửi (người gửi)
     */
    @Query("SELECT f FROM Friend f JOIN FETCH f.addressee WHERE f.requester.id = :userId AND f.status = 'PENDING' AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<Friend> findSentRequestsByUser(@Param("userId") Long userId);

    /**
     * Kiểm tra đã là bạn bè chưa
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE ((f.requester.id = :userId1 AND f.addressee.id = :userId2) OR (f.requester.id = :userId2 AND f.addressee.id = :userId1)) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL")
    boolean areFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Kiểm tra có yêu cầu kết bạn đang chờ không
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE f.requester.id = :requesterId AND f.addressee.id = :addresseeId AND f.status = 'PENDING' AND f.deletedAt IS NULL")
    boolean hasPendingRequest(@Param("requesterId") Long requesterId, @Param("addresseeId") Long addresseeId);

    /**
     * Đếm số bạn bè
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL")
    long countFriends(@Param("userId") Long userId);

    /**
     * Lấy danh sách ID bạn bè
     */
    @Query("SELECT CASE WHEN f.requester.id = :userId THEN f.addressee.id ELSE f.requester.id END FROM Friend f WHERE (f.requester.id = :userId OR f.addressee.id = :userId) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL")
    List<Long> findFriendIds(@Param("userId") Long userId);
}
