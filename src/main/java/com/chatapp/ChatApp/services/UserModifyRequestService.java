package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.model.ChatUnreadedMessageEntity;
import com.chatapp.ChatApp.model.ChatVisibilityEntity;
import com.chatapp.ChatApp.model.FriendDto;
import com.chatapp.ChatApp.model.UserEntity;
import com.chatapp.ChatApp.repository.ChatMessageRepository;
import com.chatapp.ChatApp.repository.ChatUnreadedMessageRespository;
import com.chatapp.ChatApp.repository.ChatVisibilityRepository;
import com.chatapp.ChatApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserModifyRequestService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatVisibilityRepository chatVisibilityRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatUnreadedMessageRespository chatUnreadedMessageRespository;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private SimpMessagingTemplate MessagingTemplate;

    public boolean blockOrUnblockFriend(Long myId,Long friendId,boolean block){

        UserEntity Me = userRepository.findById(myId).orElse(null);
        if(Me == null) return  false;

        UserEntity Friend = userRepository.findById(friendId).orElse(null);
        if(Friend == null) return false;

        if(block){
            Me.getBlockedList().add(Friend);
        }else{
            Me.getBlockedList().remove(Friend);
        }

        userRepository.save(Me);

        return true;
    }

    public boolean removeFriend(Long myId,Long friendId){

        UserEntity Me = userRepository.findById(myId).orElse(null);
        if(Me == null) return  false;

        UserEntity Friend = userRepository.findById(friendId).orElse(null);
        if(Friend == null) return false;

        Me.getFriends().remove(Friend);
        Friend.getFriends().remove(Me);

        userRepository.save(Me);
        userRepository.save(Friend);

        chatMessageRepository.deleteOneConversation(myId,friendId);

        ChatVisibilityEntity me = chatVisibilityRepository.getDateFromUserDeletedChat(myId,friendId);
        ChatVisibilityEntity friend = chatVisibilityRepository.getDateFromUserDeletedChat(friendId,myId);

        if(me != null){
            chatVisibilityRepository.delete(me);
        }

        if(friend != null){
            chatVisibilityRepository.delete(friend);
        }

        if(simpUserRegistry.getUser(friendId.toString()) != null){

            FriendDto friendDto = new FriendDto();
            friendDto.setNumber(Me.getNumber());

            MessagingTemplate.convertAndSendToUser(
                    friendId.toString(),
                    "/queue/incoming.friend.remove",
                    friendDto
            );

        }

        return true;
    }

    public boolean deleteChat(Long myId,Long friendId){

        LocalDateTime currentTime = LocalDateTime.now();

        ChatVisibilityEntity me = chatVisibilityRepository.getDateFromUserDeletedChat(myId,friendId);
        ChatVisibilityEntity friend = chatVisibilityRepository.getDateFromUserDeletedChat(friendId,myId);

        long chatRow = chatMessageRepository.isUserDataInDB(myId,friendId);

        if(me != null){
            me.setChatDeleted(currentTime);
            chatVisibilityRepository.save(me);
        }else if(friend == null && chatRow > 0){
            ChatVisibilityEntity createMe = new ChatVisibilityEntity();
            createMe.setMyId(myId);
            createMe.setFriendId(friendId);
            createMe.setChatDeleted(currentTime);
            chatVisibilityRepository.save(createMe);
        }

        if(friend != null){
            LocalDateTime friendTime = friend.getChatDeleted();

            chatMessageRepository.deleteUserChat(myId,friendId,friendTime);
            chatVisibilityRepository.delete(friend);

            if(me !=  null){
                chatVisibilityRepository.delete(me);
            }

        }

        return  true;
    }

    public boolean setReadLog(Long myId,Long friendId){

        ChatUnreadedMessageEntity chatUnreadedMessageEntity = chatUnreadedMessageRespository.getUser(friendId,myId);

        if(chatUnreadedMessageEntity != null){
            chatUnreadedMessageRespository.deleteUnread(friendId,myId);
        }

        return true;
    }


}
