package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.model.*;
import com.chatapp.ChatApp.repository.ChatMessageRepository;
import com.chatapp.ChatApp.repository.ChatVisibilityRepository;
import com.chatapp.ChatApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserDataRequestService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatVisibilityRepository chatVisibilityRepository;


    public boolean isActive(Long id){

        UserEntity user = userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException("No user"));

        return user.getStatus() == Status.ACTIVE;

    }

    public boolean friendRequestAlreadyExists(Long myId,Long friendId){
        return userRepository.isRequestExists(myId,friendId);
    }

    public FriendDto getUserSearchContact(Long myID,Long friendID){

        UserEntity Me = userRepository.findById(myID).orElse(null);

        if(Me == null) return null;

        if(Me.getStatus() == Status.DEACTIVE) return null;

        UserEntity Friend = userRepository.findById(friendID).orElse(null);

        if(Friend == null) return null;

        if(Friend.getStatus() == Status.DEACTIVE) return null;

        Set<UserEntity> blockedList = Friend.getBlockedList();

        for(UserEntity u : blockedList){
           if(u.getNumber().equals(myID)) return null;
        }


        return new FriendDto(Friend.getNumber(),Friend.getName(),Friend.getMail(),Friend.getProfile_url(),0,Friend.getStatus());
    }

    public boolean addFriendRequest(Long myid, Long FriendId){

        UserEntity Me = userRepository.findById(myid).orElse(null);
        if(Me == null) return  false;

        UserEntity Friend = userRepository.findById(FriendId).orElse(null);
        if(Friend == null) return false;


        Me.getFriends().add(Friend);
        Me.getRequests().remove(Friend);
        Friend.getFriends().add(Me);

        userRepository.save(Me);
        userRepository.save(Friend);

        if(simpUserRegistry.getUser(Friend.getNumber()+"") != null){

            FriendDto friendDto = new FriendDto(Me.getNumber(),Me.getName(),Me.getMail(),Me.getProfile_url(),0,Me.getStatus());

            messagingTemplate.convertAndSendToUser(
                    Friend.getNumber()+"",
                    "/queue/incoming.friend.accept",
                    friendDto
            );

        }


        return true;
    }

    public boolean giveRequestToFriend(Long myId,Long friendId){

        if(userRepository.isBlocked(friendId,myId)) return false;

        UserEntity Me = userRepository.findById(myId).orElse(null);
        if(Me == null) return  false;

        UserEntity Friend = userRepository.findById(friendId).orElse(null);
        if(Friend == null) return false;

        Friend.getRequests().add(Me);

        userRepository.save(Friend);

        if(simpUserRegistry.getUser(Friend.getNumber()+"") != null){

            FriendDto friendDto = new FriendDto(Me.getNumber(),Me.getName(),Me.getMail(),Me.getProfile_url(),0,Me.getStatus());

            messagingTemplate.convertAndSendToUser(
                    Friend.getNumber()+"",
                    "/queue/incoming.friend.request",
                    friendDto
            );

        }

        return true;
    }

    public boolean removeFriendRequest(Long myId,Long FriendId){

        UserEntity Me = userRepository.findById(myId).orElse(null);
        if(Me == null) return  false;

        UserEntity Friend = userRepository.findById(FriendId).orElse(null);
        if(Friend == null) return false;

        Me.getRequests().remove(Friend);

        userRepository.save(Me);

        return true;
    }

    public List<ChatMessageDto> getOneChatData(Long myId,Long friendId){
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();

        ChatVisibilityEntity me = chatVisibilityRepository.getDateFromUserDeletedChat(myId,friendId);

        List<ChatMessageEntity> chatMessageEntities = me == null ?chatMessageRepository.getParticularChatData(myId,friendId)
                :
                chatMessageRepository.getParticularChatData(myId,friendId,me.getChatDeleted());

        for(ChatMessageEntity chat : chatMessageEntities){
            chatMessageDtos.add(new ChatMessageDto(chat.getSender(),chat.getReceiver(),chat.getMessage()));
        }

        return chatMessageDtos;
    }


}
