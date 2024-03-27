package com.grs.api.model.request;

import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Acer on 25-Jun-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyGovRequestDTO {
    private String trackingNumber;
    private String username;
    private String secret;

    private String complainantPhoneNumber = "";
    private String name = "";
    private String email = "";

    private String serviceTrackingNumber;
    private String officeId;
    private String serviceId;
    private String submissionDate;
    private String subject;
    private String body;
    private String relation;
    private String serviceReceiver;
    private String serviceOthers;
    private Boolean isAnonymous = false;
    private ServiceType serviceType;
    private Boolean offlineGrievanceUpload = false;
    private String PhoneNumber = null;
    private Boolean isSelfMotivated;
    private String SourceOfGrievance;

    MultipartFile[] files = new MultipartFile[0];
    List<String> fileUriList = new ArrayList<>();
}
