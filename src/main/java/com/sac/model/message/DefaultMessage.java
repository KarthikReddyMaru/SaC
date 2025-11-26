package com.sac.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultMessage {

    private String content;
    private Type type;

    public enum Type {
        CHAT,
        STATE,
        WAITING_FOR_RESPONSE,
        CHOOSE
    }
}
