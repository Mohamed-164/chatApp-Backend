package com.chatapp.ChatApp.repository;
import com.chatapp.ChatApp.model.ChatUnreadedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChatUnreadedMessageRespository extends JpaRepository<ChatUnreadedMessageEntity,Long> {

    @Query("""
    SELECT m
    FROM ChatUnreadedMessageEntity m
    WHERE m.myid = :myId AND m.friendId = :FriendId
    """)
    ChatUnreadedMessageEntity getUser(Long myId,Long FriendId);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM ChatUnreadedMessageEntity c
    WHERE c.myid = :myId
    AND c.friendId = :FriendId
    """)
    void deleteUnread(Long myId, Long FriendId);


    @Modifying
    @Transactional
    @Query("""
    DELETE FROM ChatUnreadedMessageEntity c
    WHERE c.myid = :id OR c.friendId = :id
    """)
    void deleteUnreadEntry(Long id);

}
