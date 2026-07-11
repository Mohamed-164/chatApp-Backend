package com.chatapp.ChatApp.repository;

import com.chatapp.ChatApp.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity,Long> {

    @Query("""
    SELECT COUNT(m)
    FROM ChatMessageEntity m
    WHERE
    (m.sender = :myId AND m.receiver = :friendId)
    OR
    (m.sender = :friendId AND m.receiver = :myId)
    """)
    long isUserDataInDB(Long myId, Long friendId);



    @Query("""
    SELECT m FROM ChatMessageEntity m 
    WHERE
    (m.sender = :myId AND m.receiver = :friendId)
    OR
    (m.sender = :friendId AND m.receiver = :myId)
    ORDER BY m.sendTime ASC
    """)
    List<ChatMessageEntity> getParticularChatData(Long myId,Long friendId);

    @Query("""
    SELECT m
    FROM ChatMessageEntity m
    WHERE
    (
        (m.sender = :myId AND m.receiver = :friendId)
        OR
        (m.sender = :friendId AND m.receiver = :myId)
    )
    AND m.sendTime > :deletedTime
    ORDER BY m.sendTime ASC
    """)
    List<ChatMessageEntity> getParticularChatData(
                Long myId,
                Long friendId,
                LocalDateTime deletedTime
    );

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM ChatMessageEntity m
    WHERE 
    (
        (m.sender = :myId AND m.receiver = :friendId)
        OR
        (m.sender = :friendId AND m.receiver = :myId)
    )
    AND m.sendTime <= :userTime
    """)
    void deleteUserChat(Long myId,Long friendId,LocalDateTime userTime);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM ChatMessageEntity m
    WHERE 
    (
        (m.sender = :myId AND m.receiver = :friendId)
        OR
        (m.sender = :friendId AND m.receiver = :myId)
    )
    """)
    void deleteOneConversation(Long myId,Long friendId);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM ChatMessageEntity m
    WHERE m.sender = :id OR m.receiver = :id
    """)
    void deleteOneEntry(Long id);

}
