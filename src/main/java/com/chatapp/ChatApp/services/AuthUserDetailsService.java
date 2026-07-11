package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.AuthenticationDetails.AuthUserDetails;
import com.chatapp.ChatApp.model.UserEntity;
import com.chatapp.ChatApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phonenumber) throws UsernameNotFoundException {
        UserEntity UserEntity = null;
        try{
            UserEntity = userRepository.findById(Long.parseLong(phonenumber))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid data");
        }
        return new AuthUserDetails(UserEntity);
    }
}
