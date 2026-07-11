package com.chatapp.ChatApp.controller;

import com.chatapp.ChatApp.model.FriendRequestDto;
import com.chatapp.ChatApp.services.UserModifyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modify")
public class UserModifyRequestController {

    @Autowired
    private UserModifyRequestService userModifyRequestService;

    @PutMapping("/blockuser")
    public ResponseEntity<Void> requestBlockFriend(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = userModifyRequestService.blockOrUnblockFriend(friendRequestDto.getMyID(),friendRequestDto.getFriendID(),true);

        if(!response) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();

    }

    @PutMapping("/unblockuser")
    public ResponseEntity<Void> requestUnblockFriend(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = userModifyRequestService.blockOrUnblockFriend(friendRequestDto.getMyID(),friendRequestDto.getFriendID(),false);

        if(!response) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/removefriend")
    public ResponseEntity<Void> requestRemoveFriend(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = userModifyRequestService.removeFriend(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(!response) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deletechat")
    public ResponseEntity<Void> requestDeleteChat(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = userModifyRequestService.deleteChat(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(!response) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/readedMessage")
    public ResponseEntity<Void> setReadedMessageLog(@RequestBody FriendRequestDto friendRequestDto){

        boolean response = userModifyRequestService.setReadLog(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(!response) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

}
