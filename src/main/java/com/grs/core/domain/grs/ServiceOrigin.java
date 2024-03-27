package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Tanvir on 9/6/2017.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "services")
public class ServiceOrigin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "office_origin_unit_id")
    private Long officeOriginUnitId;

    @Column(name = "office_origin_unit_organogram_id")
    private Long officeOriginUnitOrganogramId;

    @Column(name = "office_origin_name_bng")
    private Long officeOriginNameBangla;

    @Column(name = "office_origin_name_eng")
    private Long officeOriginNameEnglish;

    @Column(name = "service_type")
    @Enumerated(value = EnumType.STRING)
    private ServiceType serviceType;

}
