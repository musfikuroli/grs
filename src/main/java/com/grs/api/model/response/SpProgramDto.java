package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpProgramDto {
    private Integer id;
    private String nameEn;
    private String nameBn;
    private Long officeId;
    private Boolean status;
}
