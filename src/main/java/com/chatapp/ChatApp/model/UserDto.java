package com.chatapp.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long number;
    private String name;
    private String mail;
    private String profile_url;
    private Status status;
    private Theme theme;

    private List<FriendDto> friends;
    private List<FriendDto> requests;
    private List<FriendDto> blockedlist;
    private Visibility visibility;
}
