package com.chatapp.ChatApp.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CallRequestDto {

    private String connectionType;
    private String sender;
    private String receiver;
    private JsonNode data;
}
