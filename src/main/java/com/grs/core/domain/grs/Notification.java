package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.domain.BaseEntity;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.ServiceType;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 9/14/2017.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "employee_name_eng")
    private String employeeNameEng;

    @Column(name = "employee_name_bng")
    private String employeeNameBng;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "employee_record_id")
    private Long employeeRecordId;

    @Column(name = "office_unit_organogram_id")
    private Long officeUnitOrganogramId;


    @Column(name = "complaint_id")
    private Long complaintId;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_movement_id", referencedColumnName = "id")
    private GrievanceForwarding grievanceForwarding;

    @Column(name = "is_seen")
    private Boolean isSeen;

    @Column(name = "url")
    private String url;
}
