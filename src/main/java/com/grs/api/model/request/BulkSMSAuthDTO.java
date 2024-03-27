package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 27-Mar-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkSMSAuthDTO {
    private String username;
    private String password;
    private String acode;
}
