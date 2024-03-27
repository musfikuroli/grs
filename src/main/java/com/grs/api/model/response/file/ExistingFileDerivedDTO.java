package com.grs.api.model.response.file;

import lombok.*;

/**
 * Created by Acer on 10/2/2017.
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class ExistingFileDerivedDTO extends FileBaseDTO {
    private Long id;
    private String url;
    private String thumbnailUrl;
    private String deleteUrl;
    private String deleteType;

    @Builder
    public ExistingFileDerivedDTO(Long id, String name,
                          String size,
                          String url,
                          String thumbnailUrl,
                          String deleteUrl,
                          String deleteType) {
        super(name,size);
        this.id = id;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.deleteType = deleteType;
        this.deleteUrl = deleteUrl;
    }

    public static class ExistingFileDerivedDTOBuilder extends FileBaseDTOBuilder {
        ExistingFileDerivedDTOBuilder() {
            super();
        }
    }
}
