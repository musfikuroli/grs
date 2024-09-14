package com.grs.api.model;

import com.grs.utils.CalendarUtil;
import com.grs.utils.Utility;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComplainHistory implements Serializable {

    Long complainId;
    String trackingNumber;
    String currentStatus;
    Long officeId;
    Long layerLevel;
    Long customLayer;
    Long officeOrigin;
    String mediumOfSubmission;
    String grievanceType;
    Long selfMotivated;
    Date createdAt;
    Date closedAt;

    public ComplainHistory(Object[] model) {
        if (model != null) {
            if (Utility.valueExists(model, 0)) {
                this.complainId = Utility.getLongValue(model[0]);
            }
            if (Utility.valueExists(model, 1)) {
                this.trackingNumber = (String) model[1];
            }
            if (Utility.valueExists(model, 2)) {
                this.currentStatus = (String) model[2];
            }
            if (Utility.valueExists(model, 3)) {
                this.officeId = Utility.getLongValue(model[3]);
            }
            if (Utility.valueExists(model, 4)) {
                this.layerLevel = Utility.getLongValue(model[4]);
            }
            if (Utility.valueExists(model, 5)) {
                this.customLayer = Utility.getLongValue(model[5]);
            }
            if (Utility.valueExists(model, 6)) {
                this.officeOrigin = Utility.getLongValue(model[6]);
            }
            if (Utility.valueExists(model, 7)) {
                this.mediumOfSubmission = (String) model[7];
            }
            if (Utility.valueExists(model, 8)) {
                this.grievanceType = (String) model[8];
            }
            if (Utility.valueExists(model, 9)) {
                this.selfMotivated = Utility.getLongValue(model[9]);
            }
            // if (Utility.valueExists(model, 10)) {
            //     this.createdAt = (Date) model[10];
            // }
            // if (Utility.valueExists(model, 11)) {
            //     this.closedAt = (Date) model[11];
            // }
            // Handle createdAt field and set time to 00:00:00
            if (Utility.valueExists(model, 10)) {
                this.createdAt = CalendarUtil.truncateDate((Date) model[10]);
            }
            // Handle closedAt field and set time to 00:00:00
            if (Utility.valueExists(model, 11)) {
                this.closedAt = CalendarUtil.truncateDate((Date) model[11]);
            }
        }
    }

    public com.grs.core.domain.grs.ComplainHistory getComplainHistory() {
        com.grs.core.domain.grs.ComplainHistory complainHistoryEO = new com.grs.core.domain.grs.ComplainHistory();
        complainHistoryEO.setComplainId(this.complainId);
        complainHistoryEO.setTrackingNumber(this.trackingNumber);
        complainHistoryEO.setCurrentStatus(this.currentStatus);
        complainHistoryEO.setOfficeId(this.officeId);
        complainHistoryEO.setLayerLevel(this.layerLevel);
        complainHistoryEO.setCustomLayer(this.customLayer);
        complainHistoryEO.setOfficeOrigin(this.officeOrigin);
        complainHistoryEO.setMediumOfSubmission(this.mediumOfSubmission);
        complainHistoryEO.setGrievanceType(this.grievanceType);
        complainHistoryEO.setSelfMotivated(this.selfMotivated);
        complainHistoryEO.setCreatedAt(this.createdAt);
        complainHistoryEO.setClosedAt(this.closedAt);
        return complainHistoryEO;
    }

}
