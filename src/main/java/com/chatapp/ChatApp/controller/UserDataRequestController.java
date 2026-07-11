package com.chatapp.ChatApp.controller;

import com.chatapp.ChatApp.model.ChatMessageDto;
import com.chatapp.ChatApp.model.FriendDto;
import com.chatapp.ChatApp.model.FriendRequestDto;
import com.chatapp.ChatApp.services.UserDataRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class UserDataRequestController {

    @Autowired
    private UserDataRequestService userDataRequestService;

    @PostMapping("/search-contact")
    public ResponseEntity<FriendDto> getContact(@RequestBody FriendRequestDto friendRequestDto){

        if(userDataRequestService.friendRequestAlreadyExists(friendRequestDto.getMyID(),friendRequestDto.getFriendID())){
            FriendDto friendDto = userDataRequestService.getUserSearchContact(friendRequestDto.getMyID(),friendRequestDto.getFriendID());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(friendDto);

        }

        FriendDto friendDto = userDataRequestService.getUserSearchContact(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(friendDto == null) return ResponseEntity.notFound().build();

        if(!userDataRequestService.isActive(friendRequestDto.getFriendID())) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(friendDto);

    }

    @PostMapping("/addfriend")
    public ResponseEntity<Void> addFriend(@RequestBody FriendRequestDto friendRequestDto){

        boolean updated = userDataRequestService.giveRequestToFriend(friendRequestDto.getMyID(),friendRequestDto.getFriendID());

        if(!updated) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/getChat")
    public ResponseEntity<List<ChatMessageDto>> getOneChatData(@RequestParam("myId") Long myId, @RequestParam("friendId") Long friendId){

        return ResponseEntity.ok(userDataRequestService.getOneChatData(myId,friendId));
    }

}
