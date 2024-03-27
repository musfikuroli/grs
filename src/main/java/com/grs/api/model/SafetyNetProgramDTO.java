package com.grs.api.model;

import com.grs.utils.Utility;
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
public class SafetyNetProgramDTO implements Serializable {
    String programNameEn;
    String programNameBn;
    String districtEn;
    String districtBn;
    String thanaEn;
    String thanaBn;
    Integer submitted;
    Integer resolved;
    Integer overdue;

    public SafetyNetProgramDTO(Object[] report, boolean isInner) {
        if (report != null && report.length > 0) {
            if (!isInner) {
                if (Utility.valueExists(report, 0)) {
                    this.programNameEn = (String) report[0];
                }
                if (Utility.valueExists(report, 1)) {
                    this.programNameBn = (String) report[1];
                }
                if (Utility.valueExists(report, 2)) {
                    this.submitted = Utility.getLongValue(report[2]).intValue();
                }
                if (Utility.valueExists(report, 3)) {
                    this.resolved = Utility.getLongValue(report[3]).intValue();
                }
                if (Utility.valueExists(report, 4)) {
                    this.overdue = Utility.getLongValue(report[4]).intValue();
                }

            } else {
                if (Utility.valueExists(report, 0)) {
                    this.districtEn = (String) report[0];
                }
                if (Utility.valueExists(report, 1)) {
                    this.districtBn = (String) report[1];
                }
                if (Utility.valueExists(report, 2)) {
                    this.thanaEn = (String) report[2];
                }
                if (Utility.valueExists(report, 3)) {
                    this.thanaBn = (String) report[3];
                }
                if (Utility.valueExists(report, 4)) {
                    this.submitted = Utility.getLongValue(report[4]).intValue();
                }
                if (Utility.valueExists(report, 5)) {
                    this.resolved = Utility.getLongValue(report[5]).intValue();
                }
                if (Utility.valueExists(report, 6)) {
                    this.overdue = Utility.getLongValue(report[6]).intValue();
                }
            }
        }
    }
}
