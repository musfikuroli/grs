package com.grs.api.model.response.officeSelection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DropDownItemDTO {
    private Long id;
    private String nameBangla;
    private String nameEnglish;
    private boolean selected;
}
