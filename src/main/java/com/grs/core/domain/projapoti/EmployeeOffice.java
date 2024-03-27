package com.grs.core.domain.projapoti;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by Acer on 9/7/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employee_offices")
public class EmployeeOffice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", referencedColumnName = "id")
    private Office office;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_unit_id", referencedColumnName = "id")
    private OfficeUnit officeUnit;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_unit_organogram_id", referencedColumnName = "id")
    private OfficeUnitOrganogram officeUnitOrganogram;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_record_id", referencedColumnName = "id")
    private EmployeeRecord employeeRecord;

    @Column(name = "identification_number")
    private String identificationNumber;

    @Column(name = "designation")
    private String designation;

    @Column(name = "incharge_label")
    private String inchargeLabel;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "office_head")
    private Boolean isOfficeHead;

    @Column(name = "is_default_role")
    private Boolean isDefaultRole;

}
