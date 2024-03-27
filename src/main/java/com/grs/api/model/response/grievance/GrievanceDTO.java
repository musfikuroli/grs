package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 9/17/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceDTO {
    private String id;
    private String dateEnglish;
    private String dateBangla;
    private String subject;
    private String trackingNumberEnglish;
    private String trackingNumberBangla;
    private String caseNumberEnglish;
    private String caseNumberBangla;
    private String submissionDateEnglish;
    private String submissionDateBangla;
    private String typeBangla;
    private String typeEnglish;
    private String statusBangla;
    private String statusEnglish;
    private String serviceNameEnglish;
    private String serviceNameBangla;
    private String serviceOfficerPostEnglish;
    private String serviceOfficerPostBangla;
    private String officeUnitNameEnglish;
    private String officeUnitNameBangla;
    private String expectedDateOfClosingEnglish;
    private String expectedDateOfClosingBangla;
    private Boolean isSeen;
    private Boolean isCC;
    private Boolean isExpired;
    private Float rating;
    private Float appealRating;
    private String feedbackComments;
    private String appealFeedbackComments;
    private boolean safetyNet;
    private String subType;
    private String complaintCategoryDetails;
    private String complaintGeoLocation;
}
