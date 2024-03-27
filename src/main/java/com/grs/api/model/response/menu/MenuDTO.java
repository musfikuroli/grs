package com.grs.api.model.response.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;

/**
 * Created by Acer on 9/11/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDTO {
    String nameEnglish;
    String nameBangla;
    HashSet<SubMenuDTO> subMenus;
}
