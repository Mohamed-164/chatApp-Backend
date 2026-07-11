package com.chatapp.ChatApp.controller;

import com.chatapp.ChatApp.model.*;
import com.chatapp.ChatApp.repository.ChatMessageRepository;
import com.chatapp.ChatApp.repository.ChatUnreadedMessageRespository;
import com.chatapp.ChatApp.repository.UserRepository;
import com.chatapp.ChatApp.services.UserDataRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;

@Controller
public class chatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private UserDataRequestService userDataRequestService;

    @Autowired
    private ChatUnreadedMessageRespository chatUnreadedMessageRespository;


    @MessageMapping("/message.typing")
    public void sendTyping(TypingDto typingDto){

        if(!userRepository.isBlocked(typingDto.getReceiver(),typingDto.getSender())){

            if(!userRepository.isBlocked(typingDto.getSender(),typingDto.getReceiver()) &&
                    userDataRequestService.isActive(typingDto.getSender()) && userDataRequestService.isActive(typingDto.getReceiver())
            ){

                if(simpUserRegistry.getUser(typingDto.getReceiver().toString()) != null){

                    messagingTemplate.convertAndSendToUser(
                            typingDto.getReceiver().toString(),
                            "/queue/message.typing",
                            typingDto
                    );

                }

            }

        }


    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDto chatMessage) {

        if(!userRepository.isBlocked(chatMessage.getReceiver(),chatMessage.getSender())){

            if(!userRepository.isBlocked(chatMessage.getSender(),chatMessage.getReceiver()) &&
                    userDataRequestService.isActive(chatMessage.getSender()) && userDataRequestService.isActive(chatMessage.getReceiver())
            ){

                ChatMessageEntity chat = new ChatMessageEntity();
                chat.setSender(chatMessage.getSender());
                chat.setReceiver(chatMessage.getReceiver());
                chat.setMessage(chatMessage.getMessage());
                chat.setSendTime(LocalDateTime.now());

                chatMessageRepository.save(chat);

                ChatUnreadedMessageEntity chatUnreadedMessageEntity = chatUnreadedMessageRespository.getUser(
                            chatMessage.getSender(),chatMessage.getReceiver()
                );

                if(chatUnreadedMessageEntity != null){

                    chatUnreadedMessageEntity.setCount(chatUnreadedMessageEntity.getCount()+1);
                    chatUnreadedMessageRespository.save(chatUnreadedMessageEntity);

                }else{

                    ChatUnreadedMessageEntity c = new ChatUnreadedMessageEntity();
                    c.setMyid(chatMessage.getSender());
                    c.setFriendId(chatMessage.getReceiver());
                    c.setCount(1);

                    chatUnreadedMessageRespository.save(c);

                    chatUnreadedMessageEntity = c;


                }
                if(simpUserRegistry.getUser(chatMessage.getReceiver()+"") != null){


                    messagingTemplate.convertAndSendToUser(
                            ""+chatMessage.getReceiver(),
                            "/queue/messages",
                            chatMessage
                    );

                    FriendUnreadMessageDto friendUnreadMessageDto = new FriendUnreadMessageDto(chatMessage.getSender(),chatUnreadedMessageEntity.getCount());

                    messagingTemplate.convertAndSendToUser(
                            ""+chatMessage.getReceiver(),
                            "/queue/message.unread",
                            friendUnreadMessageDto
                    );

                }


            }

        }

    }

    @MessageMapping("/chat.call.offer")
    public void sendOffer(CallRequestDto callRequestDto){

        if(simpUserRegistry.getUser(callRequestDto.getReceiver()) == null){

            messagingTemplate.convertAndSendToUser(
                    callRequestDto.getSender(),
                    "/queue/chat.call.offline",
                    ""
            );

            return;

        }

        long myId = Long.parseLong(callRequestDto.getSender());
        long friendId = Long.parseLong(callRequestDto.getReceiver());

        if(!userRepository.isBlocked(friendId,myId) &&
        userDataRequestService.isActive(myId) && userDataRequestService.isActive(friendId)){

            if(!userRepository.isBlocked(myId,friendId)){

            messagingTemplate.convertAndSendToUser(
                    callRequestDto.getReceiver(),
                    "/queue/chat.call.offer",
                    callRequestDto);
            }

        }else{
            messagingTemplate.convertAndSendToUser(
                    callRequestDto.getSender(),
                    "/queue/chat.call.blocked",
                    ""
            );
        }
    }

    @MessageMapping("/chat.call.answer")
    public void sendAnswer(CallRequestDto callRequestDto){
        messagingTemplate.convertAndSendToUser(
                callRequestDto.getReceiver(),
                "/queue/chat.call.answer",
                callRequestDto);
    }

    @MessageMapping("/chat.call.icecandidate")
    public void sendIceCandidates(CallRequestDto callRequestDto){

        long myId = Long.parseLong(callRequestDto.getSender());
        long friendId = Long.parseLong(callRequestDto.getReceiver());
        if(!userRepository.isBlocked(friendId,myId) &&
        userDataRequestService.isActive(myId) && userDataRequestService.isActive(friendId)){
            if(!userRepository.isBlocked(myId,friendId)){
                messagingTemplate.convertAndSendToUser(
                        callRequestDto.getReceiver(),
                        "/queue/chat.call.icecandidate",
                        callRequestDto);
            }
        }
    }

    @MessageMapping("/chat.call.hangup")
    public void sendHangup(CallRequestDto callRequestDto){

        long myId = Long.parseLong(callRequestDto.getSender());
        long friendId = Long.parseLong(callRequestDto.getReceiver());

        if(!userRepository.isBlocked(friendId,myId) &&
        userDataRequestService.isActive(myId) && userDataRequestService.isActive(friendId)){

            messagingTemplate.convertAndSendToUser(
                    callRequestDto.getReceiver(),
                    "/queue/chat.call.hangup",
                    callRequestDto
            );
        }

    }

    @MessageMapping("/chat.call.decline")
    public void sendDecline(CallRequestDto callRequestDto){

        messagingTemplate.convertAndSendToUser(
                callRequestDto.getReceiver(),
                "/queue/chat.call.decline",
                callRequestDto
        );

    }

    @MessageMapping("/status.ping")
    public void ping(FriendRequestDto friendRequestDto){

        UserEntity user = userRepository.findById(friendRequestDto.getMyID()).orElse(null);

        UserEntity friend = userRepository.findById(friendRequestDto.getFriendID()).orElse(null);

        if(user != null && user.getStatus() != Status.DEACTIVE &&
                friend != null && friend.getStatus() != Status.DEACTIVE &&
                user.getVisibility() != Visibility.HIDE &&
                friend.getVisibility() != Visibility.HIDE
        ){

            if(!userRepository.isBlocked(friend.getNumber(),user.getNumber()) &&
                    !userRepository.isBlocked(user.getNumber(), friend.getNumber())
            )
            {

                if(simpUserRegistry.getUser(friendRequestDto.getFriendID()+"") != null){
                    messagingTemplate.convertAndSendToUser(
                            friendRequestDto.getMyID()+"",
                            "/queue/incoming.friend.online",
                            new SocketPingDto(friendRequestDto.getFriendID())
                    );
                }
            }
        }


    }

    @Transactional
    @EventListener
    public void onConnect(SessionConnectedEvent event){

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal id = accessor.getUser();

        if(id != null){

            try {
                UserEntity user = userRepository.findById(Long.parseLong(id.getName())).orElse(null);

                if(user != null && user.getStatus() != Status.DEACTIVE && user.getVisibility() != Visibility.HIDE){

                    Set<UserEntity> friends = user.getFriends();

                    for(UserEntity u : friends){

                        if(!userRepository.isBlocked(u.getNumber(),user.getNumber())){

                            if(!userRepository.isBlocked(user.getNumber(),u.getNumber()) &&
                                    userDataRequestService.isActive(u.getNumber()) &&
                                    u.getVisibility() != Visibility.HIDE
                            ){

                                messagingTemplate.convertAndSendToUser(
                                        u.getNumber()+"",
                                        "/queue/incoming.friend.online",
                                        new SocketPingDto(user.getNumber())
                                );
                            }

                        }

                    }

                }

            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }

        }

    }

    @Transactional
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event){

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal id = accessor.getUser();

        if(id != null){

            try {
                UserEntity user = userRepository.findById(Long.parseLong(id.getName())).orElse(null);

                if(user != null && user.getStatus() != Status.DEACTIVE && user.getVisibility() != Visibility.HIDE){

                    Set<UserEntity> friends = user.getFriends();

                    for(UserEntity u : friends){

                        if(!userRepository.isBlocked(u.getNumber(),user.getNumber())){

                            if(!userRepository.isBlocked(user.getNumber(),u.getNumber()) &&
                                    userDataRequestService.isActive(u.getNumber()) &&
                                    u.getVisibility() != Visibility.HIDE
                            ){

                                messagingTemplate.convertAndSendToUser(
                                        u.getNumber()+"",
                                        "/queue/incoming.friend.offline",
                                        new SocketPingDto(user.getNumber())
                                );
                            }

                        }

                    }

                }

            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }

        }

    }


}