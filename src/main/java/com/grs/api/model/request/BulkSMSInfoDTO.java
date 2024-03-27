package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 27-Mar-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkSMSInfoDTO {
    private String message;
    private String is_unicode;
    private String masking;
    private List<String> msisdn;
}
