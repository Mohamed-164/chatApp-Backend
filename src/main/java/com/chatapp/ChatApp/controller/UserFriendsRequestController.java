package com.chatapp.ChatApp.controller;

import com.chatapp.ChatApp.model.FriendRequestDto;
import com.chatapp.ChatApp.services.UserDataRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/request")
public class UserFriendsRequestController {

    @Autowired
    private UserDataRequestService service;

    @PostMapping("/addfriend")
    public ResponseEntity<Void> addFriendRequest(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = service.addFriendRequest(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(!response) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rejectfriend")
    public ResponseEntity<Void> removeFriendRequest(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = service.removeFriendRequest(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(!response) return ResponseEntity.internalServerError().build();


        return ResponseEntity.ok().build();
    }


}
