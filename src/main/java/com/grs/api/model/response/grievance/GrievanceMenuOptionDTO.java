package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 18-Oct-17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrievanceMenuOptionDTO {
    private String iconLink;
    private String nameBangla;
    private String nameEnglish;
    private String link;
}
