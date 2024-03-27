package com.grs.api.model.response.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 10/2/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileContainerDTO {
    private List<FileBaseDTO> files;
}
