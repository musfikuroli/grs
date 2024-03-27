package com.grs.api.model.response.officeSelection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeSearchContentsDTO {
    Integer layerLevel;
    List<DropDownItemDTO> firstSelectionList;
    List<DropDownItemDTO> secondSelectionList;
}
