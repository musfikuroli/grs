package com.grs.api.model;

import com.grs.utils.Defs;
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
public class SafetyNetSummaryDTO implements Serializable {

    String subType;
    long january;
    long february;
    long march;
    long april;
    long may;
    long june;
    long july;
    long august;
    long september;
    long october;
    long november;
    long december;

    public SafetyNetSummaryDTO(Object[] result) {
        if (result != null && result.length >0) {
            if (Utility.valueExists(result, 0)) {
                this.subType = Defs.ERROR_MAP.get((String) result[0]);
            }
            if (Utility.valueExists(result, 1)) {
                this.january = Utility.getLongValue(result[1]);
            }
            if (Utility.valueExists(result, 2)) {
                this.february = Utility.getLongValue(result[2]);
            }
            if (Utility.valueExists(result, 3)) {
                this.march = Utility.getLongValue(result[3]);
            }
            if (Utility.valueExists(result, 4)) {
                this.april = Utility.getLongValue(result[4]);
            }
            if (Utility.valueExists(result, 5)) {
                this.march = Utility.getLongValue(result[5]);
            }
            if (Utility.valueExists(result, 6)) {
                this.june = Utility.getLongValue(result[6]);
            }
            if (Utility.valueExists(result, 7)) {
                this.july = Utility.getLongValue(result[7]);
            }
            if (Utility.valueExists(result, 8)) {
                this.august = Utility.getLongValue(result[8]);
            }
            if (Utility.valueExists(result, 9)) {
                this.september = Utility.getLongValue(result[9]);
            }
            if (Utility.valueExists(result, 10)) {
                this.october = Utility.getLongValue(result[10]);
            }
            if (Utility.valueExists(result, 11)) {
                this.november = Utility.getLongValue(result[11]);
            }
            if (Utility.valueExists(result, 12)) {
                this.december = Utility.getLongValue(result[12]);
            }
        }
    }
}
