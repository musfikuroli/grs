package com.grs.api.model.response;

import com.grs.core.domain.grs.Grievance;
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
public class GrievanceAdminDTO implements Serializable {

    Long id;
    String referenceNumber;
    String subject;

    public GrievanceAdminDTO(Grievance source) {
        this.id = source.getId();
        this.referenceNumber = source.getTrackingNumber();
        this.subject = source.getSubject();
    }
}
