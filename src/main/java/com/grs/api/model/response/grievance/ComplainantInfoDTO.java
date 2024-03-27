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
public class ComplainantInfoDTO {
    private Long id;
    private String name;
    private String mobileNumber;
    private String nationalId;
    private String email;
    private String presentAddress;
    private String permanentAddress;
    private String occupation;
    private String dateOfBirth;
    private String guardianName;
    private String motherName;
    private boolean isBlacklisted;
    private boolean isRequested;
}
