package com.grs.api.model.response.file;

import lombok.*;

/**
 * Created by Acer on 10/2/2017.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class FileActualDerivedWithErrorDTO extends FileDerivedDTO {
    private String error;

    @Builder
    public FileActualDerivedWithErrorDTO(String name, String size, String error) {
        super(name, size, "", "", "");
        this.error = error;
    }

    public static class FileActualDerivedWithErrorDTOBuilder extends FileDerivedDTOBuilder {
        FileActualDerivedWithErrorDTOBuilder() {
            super();
        }
    }
}
