package com.grs.api.model;

import com.grs.utils.Utility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SafetyNetProgramReportResponse implements Serializable {
    boolean status;
    String message;

    List<SafetyNetProgramDTO> data;

    public SafetyNetProgramReportResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public SafetyNetProgramReportResponse(List<SafetyNetProgramDTO> data) {
        this.status = true;
        this.data = data;
    }
}
