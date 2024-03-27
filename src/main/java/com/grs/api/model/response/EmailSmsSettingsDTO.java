package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 3/13/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailSmsSettingsDTO {
    private Long id;
    private String type;
    private String username;
    private String password;
    private String host;
    private Long port;
    private String smtpHost;
    private String url;
    private String ms_prefix;
    private Boolean disabled;
}
