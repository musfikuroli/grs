package com.grs.api.model.response;

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
public class NewBulkSmsResponse implements Serializable {

    public String status;
    public String scode;
    public String details;
    public String server;
    public String sms_class;
    public String processing_details;
    public String credit_deducted;
    public String current_credit;
    public String credit_inheritance;
    public String chunk_id;
    public String cid;
}
