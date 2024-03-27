package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dashboard_data")
public class DashboardData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "complaint_id")
    private Long grievanceId;

    @Column(name = "complainant_id")
    private Long complainantId;

    @Column(name = "submission_date")
    private Date submissionDate;

    @Column(name = "complaint_status")
    @Enumerated(value = EnumType.STRING)
    private GrievanceCurrentStatus complaintStatus;

    @Column(name = "case_number")
    private String caseNumber;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "layer_level")
    private Long layerLevel;

    @Column(name = "office_unit_id")
    private Long officeUnitId;

    @Column(name = "appeal_from_office_id")
    private Long appealFromOfficeId;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_service_id", referencedColumnName = "id")
    private CitizenCharter citizenCharter;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "grievance_type")
    @Enumerated(value = EnumType.STRING)
    private ServiceType grievanceType;

    @Column(name = "subject")
    private String subject;

    @Column(name = "accepted_date")
    private Date acceptedDate;

    @Column(name = "closed_date")
    private Date closedDate;

    @Column(name = "is_forwarded")
    private Boolean forwarded;

    @Column(name = "gro_decision")
    private String groDecision;

    @Column(name = "gro_identified_complaint_cause")
    private String groIdentifiedCause;

    @Column(name = "gro_suggestion")
    private String groSuggestion;

    @Column(name = "ao_suggestion")
    private String aoSuggestion;

    @Column(name = "ao_decision")
    private String aoDecision;

    @Column(name = "ao_identified_complaint_cause")
    private String aoIdentifiedCause;

    @Column(name = "closure_date")
    private Date closureDate;

    @Column(name = "medium_of_submission")
    @Enumerated(value = EnumType.STRING)
    private MediumOfSubmission mediumOfSubmission;

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "rating")
    private Float rating;

}
