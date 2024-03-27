package com.grs.core.domain.projapoti;

import com.grs.core.service.OfficeService;
import com.grs.utils.BeanUtil;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by Acer on 9/6/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_unit_organograms")
public class OfficeUnitOrganogram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_id")
    private Long officeId;

    @Transient
    @Getter(AccessLevel.NONE)
    private OfficeUnit officeUnit;

    @Column(name = "office_unit_id")
    private Long officeUnitId;

    @Column(name = "designation_eng")
    private String designationEnglish;

    @Column(name = "designation_bng")
    private String designationBangla;

    @Transient
    @Getter(AccessLevel.NONE)
    private Office office;

    @Column(name = "ref_origin_unit_org_id")
    private Long refOriginUnitOrgId;

    @Column(name = "is_admin")
    public Boolean isAdmin;

    @Column(name = "status")
    public Boolean status;

    public void setOffice(Office office) {
        this.setOfficeId(office.getId());
    }

    public Office getOffice() {
        OfficeService officeService = BeanUtil.bean(OfficeService.class);
        return officeService.findOne(this.getOfficeId());
    }

    public void setOfficeUnit(OfficeUnit officeUnit) {
        this.setOfficeUnitId(officeUnit.getId());
    }

    public OfficeUnit getOfficeUnit() {
        OfficeService officeService = BeanUtil.bean(OfficeService.class);
        return officeService.getOfficeUnitByIdIncludingFakeOfficeUnitForCell(this.getOfficeUnitId());
    }
}
