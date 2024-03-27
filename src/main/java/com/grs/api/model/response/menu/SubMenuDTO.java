package com.grs.api.model.response.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 9/12/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubMenuDTO implements Comparable<SubMenuDTO> {
    private String nameBangla;
    private String nameEnglish;
    private String link;

    @Override
    public int compareTo(SubMenuDTO subMenuDTO) {
        return this.nameBangla.compareTo(subMenuDTO.getNameBangla());
    }
}
