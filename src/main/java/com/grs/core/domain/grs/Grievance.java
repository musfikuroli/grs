package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.grs.core.domain.BaseEntity;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.ServiceType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "complaints")
public class Grievance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @CreationTimestamp
    @Column(name = "submission_date")
    private Date submissionDate;

    @Column(name = "complaint_type")
    @Enumerated(value = EnumType.STRING)
    private ServiceType grievanceType;

    @Column(name = "current_status")
    @Enumerated(value = EnumType.STRING)
    private GrievanceCurrentStatus grievanceCurrentStatus;

    @Column(name = "subject")
    private String subject;

    @Column(name = "details")
    private String details;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "office_id")
    private Long officeId;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", referencedColumnName = "ID")
    private ServiceOrigin serviceOrigin;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id_before_forward", referencedColumnName = "ID")
    private ServiceOrigin serviceOriginBeforeForward;

    @OneToMany(mappedBy = "grievance", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<AttachedFile> attachedFiles;

    @Column(name = "complainant_id")
    private Long complainantId;

    @Column(name = "is_grs_user")
    private boolean grsUser;

    @Column(name = "is_anonymous")
    private boolean isAnonymous;

    @Column(name = "case_number")
    private String caseNumber;

    @Column(name = "other_service")
    private String otherService;

    @Column(name = "other_service_before_forward")
    private String otherServiceBeforeForward;

    @Column(name = "service_receiver")
    private String serviceReceiver;

    @Column(name = "service_receiver_relation")
    private String serviceReceiverRelation;

    @Column(name = "current_appeal_office_id")
    private Long currentAppealOfficeId;

    @Column(name = "send_to_ao_office_id")
    private Long sendToAoOfficeId;

    @Column(name = "current_appeal_office_unit_organogram_id")
    private Long currentAppealOfficerOfficeUnitOrganogramId;

    @Column(name = "gro_decision")
    private String groDecision;

    @Column(name = "gro_identified_complaint_cause")
    private String groIdentifiedCause;

    @Column(name = "gro_suggestion")
    private String groSuggestion;

    @Column(name = "ao_decision")
    private String appealOfficerDecision;

    @Column(name = "ao_identified_complaint_cause")
    private String appealOfficerIdentifiedCause;

    @Column(name = "ao_suggestion")
    private String appealOfficerSuggestion;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "appeal_rating")
    private Float appealRating;

    @Column(name = "is_rating_given")
    private Boolean isRatingGiven;

    @Column(name = "is_appeal_rating_given")
    private Boolean isAppealRatingGiven;

    @Column(name = "feedback_comments")
    private String feedbackComments;

    @Column(name = "appeal_feedback_comments")
    private String appealFeedbackComments;

    @Column(name = "is_offline_complaint")
    private Boolean isOfflineGrievance;

    @Column(name = "uploader_office_unit_organogram_id")
    private Long uploaderOfficeUnitOrganogramId;

    @Column(name = "is_self_motivated_grievance")
    private Boolean isSelfMotivatedGrievance;

    @Column(name = "source_of_grievance")
    private String sourceOfGrievance;

    @Column(name = "is_safety_net")
    private boolean safetyNet;

    @Transient
    private String officeLayers;

    @Column(name = "complaint_category")
    private Integer complaintCategory;

    @Column(name = "sp_programme_id")
    private Integer spProgrammeId;

    @Column(name = "geo_division_id")
    private Integer geoDivisionId;

    @Column(name = "geo_district_id")
    private Integer geoDistrictId;

    @Column(name = "geo_upazila_id")
    private Integer geoUpazilaId;
    @Column(name = "medium_of_submission")
    private String mediumOfSubmission;
}
