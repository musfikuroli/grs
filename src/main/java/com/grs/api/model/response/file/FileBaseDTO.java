package com.grs.api.model.response.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 10/2/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileBaseDTO {
    private String name;
    private String size;
}
