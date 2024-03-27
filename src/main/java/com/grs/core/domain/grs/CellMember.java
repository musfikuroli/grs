package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by Acer on 11-Mar-18.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cell_members")
public class CellMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "office_unit_organogram_id")
    private Long officeUnitOrganogramId;

    @Column(name = "employee_record_id")
    private Long employeeRecordId;

    @Column(name = "cell_office_unit_organogram_id")
    private Long cellOfficeUnitOrganogramId;

    @Column(name = "is_gro")
    private Boolean isGro;

    @Column(name = "is_ao")
    private Boolean isAo;

}
