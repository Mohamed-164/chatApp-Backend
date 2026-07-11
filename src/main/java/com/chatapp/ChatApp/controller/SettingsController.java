package com.chatapp.ChatApp.controller;

import com.chatapp.ChatApp.model.ChangePasswordDto;
import com.chatapp.ChatApp.model.SettingsRequestDto;
import com.chatapp.ChatApp.model.Theme;
import com.chatapp.ChatApp.model.Visibility;
import com.chatapp.ChatApp.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
    @RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SettingsService settingsService;

    @PostMapping("/theme")
    public ResponseEntity<Void> setTheme(@RequestBody SettingsRequestDto srDto){

        Theme theme;

        try{
            theme = Theme.valueOf(srDto.getEnums());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        boolean done = settingsService.setTheme(srDto.getNumber(),theme);

        if(done) return ResponseEntity.ok().build();


        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/visibility")
    public ResponseEntity<Void> setVisibility(@RequestBody SettingsRequestDto srDto){

        Visibility visibility;

        try {
            visibility = Visibility.valueOf(srDto.getEnums());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        boolean done = settingsService.setVisibility(srDto.getNumber(),visibility);

        if(done) return ResponseEntity.ok().build();

        return ResponseEntity.internalServerError().build();

    }

    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDto cpDto){

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        cpDto.getNumber(),
                        cpDto.getOldPassword()
                )
        );

        boolean process = settingsService.updatePassword(cpDto.getNumber(),cpDto.getNewPassword());

        if(!process) return ResponseEntity.internalServerError().build();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/active")
    public ResponseEntity<Void> setActive(@RequestBody SettingsRequestDto srDto){

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        srDto.getNumber(),
                        srDto.getPassword()
                )
        );

        if(settingsService.setActive(srDto.getNumber())) return ResponseEntity.ok().build();

        return ResponseEntity.internalServerError().build();

    }

    @PostMapping("/deactive")
    public ResponseEntity<Void> setDeActive(@RequestBody SettingsRequestDto srDto){

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        srDto.getNumber(),
                        srDto.getPassword()
                )
        );

        if(settingsService.setDeActive(srDto.getNumber())) return ResponseEntity.ok().build();

        return ResponseEntity.internalServerError().build();

    }

    @PostMapping("/editprofile")
    public ResponseEntity<Void> setProfile(@RequestParam(value = "image",required = false)MultipartFile file,
                                           @RequestParam("id") Long id,
                                           @RequestParam(value = "name",required = false) String name,
                                           @RequestParam(value = "mail",required = false) String mail
    ){


        if(file != null && !file.isEmpty()){
            String type = file.getContentType();
            String fileName = file.getOriginalFilename();
            if(type == null || !(
                    type.equals("image/jpeg") || type.equals("image/png")
                    ) ||
                    fileName == null || !(
                    fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                    )){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        boolean done = settingsService.editProfile(file,id,name,mail);

        if(done) return ResponseEntity.ok().build();


        return ResponseEntity.internalServerError().build();
    }

    @PostMapping("/removeProfile")
    public ResponseEntity<Void> removeProfile(@RequestParam(value = "id") Long id){

        boolean done = settingsService.removeProfile(id);

        if(done) return ResponseEntity.ok().build();


        return ResponseEntity.internalServerError().build();

    }

}
