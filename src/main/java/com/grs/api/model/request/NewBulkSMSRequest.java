package com.grs.api.model.request;

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
public class NewBulkSMSRequest implements Serializable {
    String op ="SMS";
    String chunk = "S";
    String user;
    String pass;
    String servername = "bulksms.teletalk.com.bd";
    String smsclass = "GENERAL";
    String sms;
    String sms_id;
    String mobile;
    String charset;
    String validity = "1440";
    String a_key;
    String p_key;
    String cid;
}
