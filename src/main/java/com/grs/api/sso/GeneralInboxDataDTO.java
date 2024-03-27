package com.grs.api.sso;

import com.grs.api.model.response.dashboard.NameValuePairDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralInboxDataDTO {
    NameValuePairDTO inbox;
    NameValuePairDTO outbox;
    NameValuePairDTO expired;
    NameValuePairDTO forwarded;
    NameValuePairDTO resolved;
    NameValuePairDTO cc;
}
