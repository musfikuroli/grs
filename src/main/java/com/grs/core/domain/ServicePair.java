package com.grs.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by User on 10/22/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServicePair {
    private ServiceType serviceType;
    private String serviceTypeValue;
}