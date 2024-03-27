package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 8/30/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseObjectDTO {
    private Long id;
    private String name;
}
