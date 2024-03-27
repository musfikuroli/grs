package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by Acer on 9/12/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "offices_citizen_charter")
public class CitizenCharter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "so_office_id")
    private Long soOfficeId;

    @Column(name = "so_office_unit_id")
    private Long soOfficeUnitId;

    @Column(name = "so_office_unit_organogram_id")
    private Long soOfficeUnitOrganogramId;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    @JsonIgnore
    private ServiceOrigin serviceOrigin;

    @Column(name = "service_name_bng")
    private String serviceNameBangla;

    @Column(name = "service_name_eng")
    private String serviceNameEnglish;

    @Column(name = "service_procedure_bng")
    private String serviceProcedureBangla;

    @Column(name = "service_procedure_eng")
    private String serviceProcedureEnglish;

    @Column(name = "documents_and_location_bng")
    private String documentAndLocationBangla;

    @Column(name = "documents_and_location_eng")
    private String documentAndLocationEnglish;

    @Column(name = "payment_method_bng")
    private String paymentMethodBangla;

    @Column(name = "payment_method_eng")
    private String paymentMethodEnglish;

    @Column(name = "service_time")
    private Integer serviceTime;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "origin_status")
    private Boolean originStatus;
}
