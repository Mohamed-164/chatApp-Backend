package com.chatapp.ChatApp.repository;

import com.chatapp.ChatApp.model.ChatVisibilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChatVisibilityRepository extends JpaRepository<ChatVisibilityEntity,Long> {

    @Query("""
    SELECT c 
    FROM ChatVisibilityEntity c
    WHERE c.myId = :myID AND c.friendId = :friendID
""")
    ChatVisibilityEntity getDateFromUserDeletedChat(Long myID,Long friendID);

    @Modifying
    @Transactional
    @Query("""
    DELETE FROM ChatVisibilityEntity c
    WHERE c.myId = :id OR c.friendId = :id
    """)
    void deleteOneEntry(Long id);

}
