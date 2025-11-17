package com.sac.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Type type;
    private String content;

    public enum Type {
        WAITING_FOR_RESPONSE, MESSAGE
    }

}
