package com.chatapp.ChatApp.repository;

import com.chatapp.ChatApp.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query(value = """
        SELECT COUNT(*) > 0
        FROM friends_blocked_list
        WHERE blocker_id = :blockerId
        AND blocked_id = :blockedId
        """, nativeQuery = true)
    boolean isBlocked(Long blockerId, Long blockedId);

    @Query(value = """
        SELECT COUNT(*) > 0
        FROM friends_request_list
        WHERE from_id = :myId
        AND to_id = :friendId
    """,nativeQuery = true)
    boolean isRequestExists(Long myId,Long friendId);

    @Query(value = """
        SELECT to_id
        FROM friends_request_list
        WHERE from_id = :myId
    """,nativeQuery = true)
    List<Long> getRequestId(Long myId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM UserEntity u 
        WHERE u.number = :id
        """)
    void deleteUser(Long id);
    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM friends_list
        WHERE (
            (user_id = :myId)
            OR
            (friend_id = :myId)
        )
    """,nativeQuery = true)
    void deleteEntryFromFriendsLists(Long myId);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE  FROM friends_blocked_list
        WHERE (
                (blocker_id = :myId)
                OR
                (blocked_id =:myId)
        )
    """,nativeQuery = true)
    void deleteEntryFromBlockedLists(Long myId);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM friends_request_list
        WHERE (
                (from_id = :myId)
                OR
                (to_id = :myId)
            )
    """,nativeQuery = true)
    void deleteEntryFromRequestsLists(Long myId);

}
