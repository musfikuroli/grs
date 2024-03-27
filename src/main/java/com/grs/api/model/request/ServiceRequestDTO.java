package com.grs.api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by User on 10/19/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequestDTO {
    private String nameBangla;
    private String nameEnglish;
    private String servingProcessBangla;
    private String servingProcessEnglish;
    private String documentAndLocationBangla;
    private String documentAndLocationEnglish;
    private String paymentMethodBangla;
    private String paymentMethodEnglish;
    private Integer serviceTime;
    private String serviceType;
}
