package com.chatapp.ChatApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_message_deleted")
public class ChatVisibilityEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false,name = "my_Id")
    private Long myId;
    @Column(nullable = false,name = "friend_Id")
    private Long friendId;
    @Column(nullable = false,name = "chat_deleted")
    private LocalDateTime chatDeleted;
}
