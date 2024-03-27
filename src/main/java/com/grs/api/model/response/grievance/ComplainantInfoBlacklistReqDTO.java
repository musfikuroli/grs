package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 12-Oct-17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplainantInfoBlacklistReqDTO {
    private Long id;
    private String name;
    private String mobileNumber;
    private String occupation;
    private boolean isBlacklisted;
    private boolean isRequested;
    private String officeName;
    private Long officeId;
    private String blacklistReason;
}
