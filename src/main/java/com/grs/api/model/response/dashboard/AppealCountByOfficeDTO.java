package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppealCountByOfficeDTO {
    Long id;
    String nameBangla;
    String nameEnglish;
    Long grievanceCount;
    Long resolvedCount;
}
