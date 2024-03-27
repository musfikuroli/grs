package com.grs.core.domain.projapoti;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_origin_units")
public class OfficeOriginUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "unit_name_bng")
    private String nameBangla;

    @Column(name = "unit_name_eng")
    private String nameEnglish;
}
