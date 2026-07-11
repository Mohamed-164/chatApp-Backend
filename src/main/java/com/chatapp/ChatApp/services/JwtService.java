package com.chatapp.ChatApp.services;

import com.chatapp.ChatApp.model.UserEntity;
import com.chatapp.ChatApp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class JwtService {

    @Autowired
    private UserRepository userRepository;

     private static final long ONE_DAY = 24L * 60 * 60 * 1000;
     private final String SECRET_KEY;

     public JwtService(){

         try {
             KeyGenerator keygen = KeyGenerator.getInstance("HmacSHA256");
             SecretKey key = keygen.generateKey();
             SECRET_KEY = Base64.getEncoder().encodeToString(key.getEncoded());
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }

     }

     public String generateToken(Long id){

         Map<String, Object> claims = new HashMap<>();

         return Jwts
                 .builder()
                 .claims(claims)
                 .subject(id.toString())
                 .issuedAt(new Date(System.currentTimeMillis()))
                 .expiration(new Date(System.currentTimeMillis()+ONE_DAY))
                 .signWith(getKey())
                 .compact();

     }

     public SecretKey getKey(){

         byte [] key = Decoders.BASE64.decode(SECRET_KEY);
         return Keys.hmacShaKeyFor(key);

     }

     public Claims extractClaims(String token){

         return  Jwts.parser()
                 .verifyWith(getKey())
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
     }

        public boolean validateToken(String token) {


            try{

                Claims claims = extractClaims(token);

                String username = claims.getSubject();
                UserEntity user = userRepository.findById(Long.parseLong(username)).orElse(null);
                if(user == null) return false;

            } catch (Exception e) {
                return false;
            }

             return !isExpired(token);
        }

    public String extractUserName(String token) {

         return extractClaims(token).getSubject();
    }

    public boolean isExpired(String token){
         return extractClaims(token).getExpiration().before(new Date());
    }

}
