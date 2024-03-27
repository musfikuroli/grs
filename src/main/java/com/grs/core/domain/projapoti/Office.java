package com.grs.core.domain.projapoti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.service.OfficeService;
import com.grs.utils.BeanUtil;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by Acer on 8/30/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "offices")
public class Office {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_ministry_id", referencedColumnName = "id")
    private OfficeMinistry officeMinistry;

    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_layer_id", referencedColumnName = "id")
    private OfficeLayer officeLayer;

    @Column(name = "parent_office_id")
    private Long parentOfficeId;

    @NotNull
    @Column(name = "office_name_eng")
    private String nameEnglish;

    @NotNull
    @Column(name = "office_name_bng")
    private String nameBangla;

    @Column(name = "geo_district_id")
    private Integer districtId;

    @Column(name = "geo_division_id")
    private Integer divisionId;

    @Column(name = "geo_upazila_id")
    private Integer upazilaId;

    @Column(name = "office_web")
    private String websiteUrl;

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "status")
    private Boolean status;

    @JsonIgnore
    @Transient
    @Getter(AccessLevel.NONE)
    private Office parentOffice;

    public void setParentOffice(Office office) {
        this.setParentOfficeId(office.getId());
    }

    public Office getParentOffice() {
        OfficeService officeService = BeanUtil.bean(OfficeService.class);
        return this.id == 0 ? null : officeService.findOne(this.getParentOfficeId());
    }
}
