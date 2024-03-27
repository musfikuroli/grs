package com.grs.api.myGov;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class MyGovTokenResponse implements Serializable {

    String id_token;
    String token_type;
    String expires_in;
    String access_token;
    String refresh_token;
}
