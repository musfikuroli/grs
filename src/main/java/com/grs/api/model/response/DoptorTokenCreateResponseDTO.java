package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 2/7/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoptorTokenCreateResponseDTO {
    private String access_token;
    private String refresh_token;
}
