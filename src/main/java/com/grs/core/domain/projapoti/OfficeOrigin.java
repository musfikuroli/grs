package com.grs.core.domain.projapoti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.service.OfficeService;
import com.grs.utils.BeanUtil;
import lombok.*;

import javax.persistence.*;

/**
 * Created by HP on 4/9/2018.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_origins")
public class OfficeOrigin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_layer_id")
    private Long officeLayerId;

    @Column(name = "office_name_eng")
    private String officeNameEnglish;

    @Column(name = "office_name_bng")
    private String officeNameBangla;

    @Column(name = "parent_office_id")
    private Long parentOfficeOriginId;

    @JsonIgnore
    @Transient
    @Getter(AccessLevel.NONE)
    private OfficeOrigin parentOfficeOrigin;

    public void setParentOfficeOrigin(OfficeOrigin officeOrigin) {
        this.setParentOfficeOriginId(officeOrigin.getId());
    }

    public OfficeOrigin getParentOfficeOrigin() {
        OfficeService officeService = BeanUtil.bean(OfficeService.class);
        return this.id == 0 ? null : officeService.getOfficeOrigin(this.getParentOfficeOriginId());
    }
}
