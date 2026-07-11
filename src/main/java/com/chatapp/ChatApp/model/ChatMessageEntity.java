package com.chatapp.ChatApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_message")
public class ChatMessageEntity {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "sender_id",nullable = false)
    private Long sender;
    @Column(name = "receiver_id",nullable = false)
    private Long receiver;
    private String message;
    @Column(name = "send_time",nullable = false)
    private LocalDateTime sendTime;
}
