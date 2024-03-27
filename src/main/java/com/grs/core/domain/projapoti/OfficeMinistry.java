package com.grs.core.domain.projapoti;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_ministries")
public class OfficeMinistry{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Range(min = 1L, max = 2L)
    @Column(name = "office_type", columnDefinition = "TINYINT(2)")
    private Integer officeType;

    @NotBlank
    @Column(name = "name_bng")
    private String nameBangla;

    @NotBlank
    @Column(name = "name_eng")
    private String nameEnglish;

    @NotBlank
    @Column(name = "name_eng_short")
    private String nameEnglishShort;

    @NotBlank
    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "status")
    private Boolean status;
}
