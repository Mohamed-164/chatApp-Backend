package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.model.*;
import com.chatapp.ChatApp.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class SettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserDataRequestService userDataRequestService;

    public void socketEmitToAllFriends(String Endpoint, UserEntity user, Object message){


        if(user != null && user.getStatus() != Status.DEACTIVE){

            Set<UserEntity> friends = user.getFriends();

            for(UserEntity u : friends){

                if(!userRepository.isBlocked(u.getNumber(),user.getNumber())){

                    if(!userRepository.isBlocked(user.getNumber(),u.getNumber()) &&
                            userDataRequestService.isActive(u.getNumber())
                    ){
                        if(message instanceof FriendDto || (user.getVisibility() != Visibility.HIDE && u.getVisibility() != Visibility.HIDE)){
                            messagingTemplate.convertAndSendToUser(
                                    u.getNumber()+"",
                                    Endpoint,
                                    message
                            );
                        }
                    }

                }

            }

        }
    }

    public boolean setTheme(Long id,Theme theme){

        UserEntity user = userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException("User not found"));

        user.setTheme(theme);
        userRepository.save(user);

        return  true;
    }

    public boolean setVisibility(Long id, Visibility visibility) {

        UserEntity user = userRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("not found"));

        user.setVisibility(visibility);
        userRepository.save(user);

        if(visibility == Visibility.SHOW){
            socketEmitToAllFriends("/queue/incoming.friend.online",user,new SocketPingDto(user.getNumber()));
        }else{
            socketEmitToAllFriends("/queue/incoming.friend.offline",user,new SocketPingDto(user.getNumber()));
        }

        return true;

    }

    public boolean updatePassword(Long id,String password){

        UserEntity user = userRepository.findById(id).orElse(null);

        if(user == null) return false;

        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        return true;
    }

    public boolean setActive(Long id){

        UserEntity user = userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException(("user not found")));

        user.setStatus(Status.ACTIVE);

        userRepository.save(user);

        socketEmitToAllFriends("/queue/incoming.friend.online",user,new SocketPingDto(user.getNumber()));

        return true;
    }

    public boolean setDeActive(Long id){

        UserEntity user = userRepository.findById(id).orElseThrow(()->new UsernameNotFoundException(("user not found")));

        user.setStatus(Status.DEACTIVE);

        userRepository.save(user);

        socketEmitToAllFriends("/queue/incoming.friend.offline",user,new SocketPingDto(user.getNumber()));

        return true;
    }

    public boolean editProfile(MultipartFile image,Long id,String name,String mail){

        UserEntity user = userRepository.findById(id).orElse(null);
        if(user == null) return  false;

        if(image != null && !image.isEmpty()){
            Map result = null;
            try {
                result = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                        "public_id",id.toString(),
                        "folder","profiles/"+id,
                        "overwrite",true
                ));
            } catch (Exception e) {
               return false;
            }

            String url = (String) result.get("secure_url");
            user.setProfile_url(url);
        }


        if(name != null){
            user.setName(name);
        }

        if(mail != null){
            user.setMail(mail);
        }

        userRepository.save(user);

        FriendDto friendDto = new FriendDto();
        friendDto.setName(user.getName());
        friendDto.setMail(user.getMail());
        friendDto.setNumber(user.getNumber());
        friendDto.setProfile_url(user.getProfile_url());

        socketEmitToAllFriends("/queue/incoming.friend.profile",user,friendDto);

        return true;
    }

    public boolean removeProfile(Long id) {

        UserEntity user = userRepository.findById(id).orElse(null);

        if(user == null) return false;

        try {
            cloudinary.uploader().destroy("profiles/"+id+"/"+id,ObjectUtils.emptyMap());
            user.setProfile_url(null);
            userRepository.save(user);

            FriendDto friendDto = new FriendDto();
            friendDto.setName(user.getName());
            friendDto.setMail(user.getMail());
            friendDto.setNumber(user.getNumber());
            friendDto.setProfile_url(user.getProfile_url());

            socketEmitToAllFriends("/queue/incoming.friend.profile",user,friendDto);

        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
