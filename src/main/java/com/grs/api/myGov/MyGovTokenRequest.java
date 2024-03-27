package com.grs.api.myGov;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyGovTokenRequest implements Serializable {
    String grant_type;
    String client_id;
    String client_secret;
    String redirect_uri;
    String code;

    public MyGovTokenRequest(String grant_type, String client_id, String client_secret, String redirect_uri, String code) {
        this.grant_type = grant_type;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.redirect_uri = redirect_uri;
        this.code = code;
    }
}
