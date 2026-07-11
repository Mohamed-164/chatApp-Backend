package com.chatapp.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendDto {
    private Long number;
    private String name;
    private String mail;
    private String profile_url;
    private int unreadMsg;
    private Status status;
}
