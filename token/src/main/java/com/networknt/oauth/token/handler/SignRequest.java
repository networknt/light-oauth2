package com.networknt.oauth.token.handler;

import java.util.Map;

public class SignRequest {
    int expires;
    Map<String, Object> payload;

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
