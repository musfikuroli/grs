package com.grs.api.model.response.file;

import lombok.*;

/**
 * Created by Acer on 10/2/2017.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class FileDerivedWithErrorDTO extends FileBaseDTO {
    private String error;

    @Builder
    public FileDerivedWithErrorDTO(String name, String size,String error) {
        super(name, size);
        this.error = error;
    }

    public static class FileDerivedWithErrorDTOBuilder extends FileBaseDTOBuilder {
        FileDerivedWithErrorDTOBuilder() {
            super();
        }
    }
}
