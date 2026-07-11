package com.chatapp.ChatApp.controller;

import com.chatapp.ChatApp.model.*;
import com.chatapp.ChatApp.services.AuthenticationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticateController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/signup")
    public ResponseEntity<Void> authSignup(@RequestBody UserEntity UserEntity){

        boolean userExists = authenticationService.signupUser(UserEntity);

        if(userExists) return ResponseEntity.status(HttpStatus.CREATED).build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getPhonenumber(),
                            dto.getPassword()
                    )
            );

            if(simpUserRegistry.getUser(dto.getPhonenumber()+"") != null){

                messagingTemplate.convertAndSendToUser(
                        dto.getPhonenumber(),
                        "/queue/incoming.conflict.login",
                        ""
                );

                 return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

        LoginResponseDto response = authenticationService.getUserData(Long.parseLong(dto.getPhonenumber()));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteaccount")
    public ResponseEntity<Void> deleteAccount(@RequestBody SettingsRequestDto settingsRequestDto){

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        settingsRequestDto.getNumber(),
                        settingsRequestDto.getPassword()
                )
        );

        try {
            boolean done = authenticationService.deleteAccount(settingsRequestDto.getNumber());

            if(!done) return ResponseEntity.notFound().build();

        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }


        return ResponseEntity.ok().build();
    }


}
