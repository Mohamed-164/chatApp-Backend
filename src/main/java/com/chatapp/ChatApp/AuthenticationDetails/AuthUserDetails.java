package com.chatapp.ChatApp.AuthenticationDetails;


import com.chatapp.ChatApp.model.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthUserDetails implements UserDetails {

    private final UserEntity UserEntity;

    public AuthUserDetails(UserEntity UserEntity){
        this.UserEntity = UserEntity;
    }

    @Override
    public String getPassword() {
        return UserEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return String.valueOf(UserEntity.getNumber());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
