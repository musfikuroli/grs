package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.RoleType;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 05-Oct-17.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "complaint_movements" )
public class GrievanceForwarding extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", referencedColumnName = "id")
    private Grievance grievance;

    @Column(name = "note")
    private String comment;

    @Column(name = "action")
    private String action;

    @Column(name = "to_employee_record_id")
    private Long toEmployeeRecordId;

    @Column(name = "from_employee_record_id")
    private Long fromEmployeeRecordId;

    @Column(name = "to_office_unit_organogram_id")
    private Long toOfficeUnitOrganogramId;

    @Column(name = "from_office_unit_organogram_id")
    private Long fromOfficeUnitOrganogramId;

    @Column(name = "to_office_id")
    private Long toOfficeId;

    @Column(name = "from_office_id")
    private Long fromOfficeId;

    @Column(name = "to_office_unit_id")
    private Long toOfficeUnitId;

    @Column(name = "from_office_unit_id")
    private Long fromOfficeUnitId;

    @Column(name = "current_status")
    @Enumerated(value = EnumType.STRING)
    private GrievanceCurrentStatus currentStatus;

    @Column(name = "deadline_date")
    private Date deadlineDate;

    @OneToMany(mappedBy = "grievanceForwarding", fetch = FetchType.LAZY)
    private List<MovementAttachedFile> attachedFiles;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "is_cc")
    private Boolean isCC;

    @Column(name = "is_committee_member")
    private Boolean isCommitteeMember;

    @Column(name = "is_committee_head")
    private Boolean isCommitteeHead;

    @Column(name = "is_seen")
    private Boolean isSeen;

    @Column(name = "to_employee_name_bng")
    private String toEmployeeNameBangla;

    @Column(name = "from_employee_name_bng")
    private String fromEmployeeNameBangla;

    @Column(name = "to_employee_name_eng")
    private String toEmployeeNameEnglish;

    @Column(name = "from_employee_name_eng")
    private String fromEmployeeNameEnglish;

    @Column(name = "to_employee_designation_bng")
    private String toEmployeeDesignationBangla;

    @Column(name = "from_employee_designation_bng")
    private String fromEmployeeDesignationBangla;

    @Column(name = "to_office_name_bng")
    private String toOfficeNameBangla;

    @Column(name = "from_office_name_bng")
    private String fromOfficeNameBangla;

    @Column(name = "to_employee_unit_name_bng")
    private String toEmployeeUnitNameBangla;

    @Column(name = "from_employee_unit_name_bng")
    private String fromEmployeeUnitNameBangla;

    @Column(name = "from_employee_username")
    private String fromEmployeeUsername;

    @Column(name = "assigned_role")
    @Enumerated(value = EnumType.STRING)
    private RoleType assignedRole;

    @Transient
    private String officeLayers;

}
