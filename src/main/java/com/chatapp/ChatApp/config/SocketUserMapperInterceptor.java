package com.chatapp.ChatApp.config;

import com.chatapp.ChatApp.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Component
public class SocketUserMapperInterceptor
        implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String username =
                    accessor.getFirstNativeHeader("username");

            String auth = accessor.getFirstNativeHeader("Authorization");

            if(auth != null && auth.startsWith("Bearer ")){

                if(!jwtService.validateToken(auth.substring(7))){
                    try {
                        throw  new AccessDeniedException("Access denied");
                    } catch (AccessDeniedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }else{
                try {
                    throw  new AccessDeniedException("Access denied");
                } catch (AccessDeniedException e) {
                    throw new RuntimeException(e);
                }
            }

            accessor.setUser(
                    () -> username
            );
        }

        return message;
    }
}