package com.chatapp.ChatApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_unreaded_message")
public class ChatUnreadedMessageEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private Long myid;
    @Column(nullable = false,name = "friendid")
    private Long friendId;
    @Column(nullable = false)
    private Integer count;
}
