package com.grs.api.model;

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
public class SafetyNetSummaryResponse implements Serializable {
    boolean status;
    String message;
    List<SafetyNetSummaryDTO> data;

    public SafetyNetSummaryResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public SafetyNetSummaryResponse(List<SafetyNetSummaryDTO> data) {
        this.status = true;
        this.data = data;
    }
}
