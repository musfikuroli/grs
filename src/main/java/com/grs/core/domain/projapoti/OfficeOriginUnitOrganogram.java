package com.grs.core.domain.projapoti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.repo.projapoti.OfficeOriginUnitRepo;
import com.grs.utils.BeanUtil;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_origin_unit_organograms")
public class OfficeOriginUnitOrganogram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_origin_unit_id")
    private Long officeOriginUnitId;

    @Column(name = "designation_bng")
    private String nameBangla;

    @Column(name = "designation_eng")
    private String nameEnglish;

}
