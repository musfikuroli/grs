package com.grs.api.model.response.menu;

import com.grs.api.model.response.menu.MenuDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 9/11/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuListDTO {
    private List<MenuDTO> menus;
}
