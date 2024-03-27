package com.grs.core.dao;

import com.grs.core.domain.grs.SafetyNetProgram;
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
public class SafetyNetDAO implements Serializable {

    String officeId;
    String officeLayer;
    String nameEn;
    String nameBn;
    String officeNameEn;
    String officeNameBn;

    public SafetyNetDAO(SafetyNetProgram program) {
        if (program != null) {
            this.officeId = program.getOfficeId()+"";
            this.officeLayer = program.getOfficeLayer()+"";
            this.nameEn = program.getNameEn();
            this.nameBn = program.getNameBn();
        }
    }
}
