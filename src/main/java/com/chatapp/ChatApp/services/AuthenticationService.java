package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.model.*;
import com.chatapp.ChatApp.repository.ChatMessageRepository;
import com.chatapp.ChatApp.repository.ChatUnreadedMessageRespository;
import com.chatapp.ChatApp.repository.ChatVisibilityRepository;
import com.chatapp.ChatApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthenticationService {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FriendsService friendsService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatVisibilityRepository chatVisibilityRepository;

    @Autowired
    private ChatUnreadedMessageRespository chatUnreadedMessageRespository;

    public boolean signupUser(UserEntity userEntity){

        UserEntity existsUser = userRepository.findById(userEntity.getNumber()).orElse(null);

        if(existsUser == null){
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            userEntity.setStatus(Status.ACTIVE);
            userEntity.setTheme(Theme.LIGHT);
            userEntity.setVisibility(Visibility.SHOW);
            userRepository.save(userEntity);
            return true;
        }

        return false;
    }

    public LoginResponseDto getUserData(Long id){

        UserEntity UserEntity = userRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        Status status = UserEntity.getStatus();

        List<FriendDto> friendslist= status == Status.ACTIVE ?
                friendsService.getAllFriendsData(UserEntity.getNumber(),UserEntity.getFriends())
                :
                new ArrayList<>();

        List<FriendDto> requestslist = status == Status.ACTIVE ?
                friendsService.getAllFriendsData(UserEntity.getNumber(),UserEntity.getRequests())
                :
                new ArrayList<>();

        List<FriendDto> blockedlist = status == Status.ACTIVE ?
                friendsService.getAllFriendsData(UserEntity.getNumber(),UserEntity.getBlockedList())
                :
                new ArrayList<>();

        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setUserDto(
                new UserDto
                        (UserEntity.getNumber(),
                                UserEntity.getName(),
                                UserEntity.getMail(),
                                UserEntity.getProfile_url(),
                                UserEntity.getStatus(),
                                UserEntity.getTheme(),
                                friendslist,
                                requestslist,
                                blockedlist,
                                UserEntity.getVisibility()
                        )
        );

        loginResponseDto.setToken(jwtService.generateToken(id));


        return loginResponseDto;
    }

    @Transactional
    public boolean deleteAccount(Long myId){

        UserEntity user = userRepository.findById(myId).orElse(null);

        if(user == null) return false;

        try {
            Set<UserEntity> friends = new HashSet<>(user.getFriends());
            List<Long> Myrequests = new ArrayList<>(userRepository.getRequestId(myId));
            chatUnreadedMessageRespository.deleteUnreadEntry(myId);
            chatVisibilityRepository.deleteOneEntry(myId);
            chatMessageRepository.deleteOneEntry(myId);
            userRepository.deleteEntryFromRequestsLists(myId);
            userRepository.deleteEntryFromBlockedLists(myId);
            userRepository.deleteEntryFromFriendsLists(myId);

            userRepository.deleteUser(myId);
            FriendDto friendDto = new FriendDto();
            friendDto.setNumber(myId);
            for(UserEntity u : friends){

                if(simpUserRegistry.getUser(u.getNumber().toString()) != null){

                    messagingTemplate.convertAndSendToUser(
                            u.getNumber().toString(),
                            "/queue/delete.friend",
                            friendDto
                    );

                }

            }

            for(Long id : Myrequests){

                if(simpUserRegistry.getUser(id.toString()) != null){

                    messagingTemplate.convertAndSendToUser(
                            id.toString(),
                            "/queue.delete.request",
                            friendDto
                    );

                }

            }


        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return true;
    }


}
