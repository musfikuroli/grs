package com.grs.utils;

import java.util.WeakHashMap;
import java.util.Map;

public class MessageUtils {

    public static Map getNotificationMessagesByGrievanceForwardingAction(String action, Long grievanceId, String trackingNumber) {
        String toText, clickAction;
        switch (action) {
            case "NEW":
                toText = "New Grievance (" + trackingNumber + ")";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            case "ACCEPTED":
                toText = "Grievance (" + trackingNumber + ") accepted";
                clickAction = "/viewGrievances.do?id=" + grievanceId + "#complaintMovementHistory";
                break;
            case "SEND_FOR_OPINION":
                toText = "Opinion asked by GRO (" + trackingNumber + ")";
                clickAction = "/viewGrievances.do?id=" + grievanceId + "#complaintMovementHistory";
                break;
            case "STATEMENT_ANSWERED":
                toText = "Statement given (" + trackingNumber + ")";
                clickAction = "/viewGrievances.do?id=" + grievanceId + "#complaintMovementHistory";
                break;
            case "REJECTED":
                toText = "Grievance (" + trackingNumber + ") has been rejected";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            case "CLOSED_ACCUSATION_PROVED":
                toText = "Grievance (" + trackingNumber + ") closed as correct";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            case "CLOSED_ACCUSATION_INCORRECT":
                toText = "Grievance (" + trackingNumber + ") closed as incorrect";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            default:
                toText = "Grievance (" + trackingNumber + ") proceeded to next step";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
        }
        WeakHashMap<String, String> result = new WeakHashMap();
        result.put("toText", toText);
        result.put("clickAction", clickAction);
        return result;
    }
}
