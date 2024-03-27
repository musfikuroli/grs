package com.grs.core.domain.grs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "grs_statistics")
public class GrsStatistics implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "office_id")
    Long officeId;

    @Column(name = "year")
    Integer year;

    @Column(name = "month")
    Integer month;

    @Column(name = "total_submitted_grievance")
    Long totalSubmittedGrievance;

    @Column(name = "current_month_acceptance")
    Long currentMonthAcceptance;

    @Column(name = "ascertain_of_last_month")
    Long ascertainOfLastMonth;

    @Column(name = "running_grievances")
    Long runningGrievances;

    @Column(name = "forwarded_grievances")
    Long forwardedGrievances;

    @Column(name = "time_expired_grievances")
    Long timeExpiredGrievances;

    @Column(name = "resolved_grievances")
    Long resolvedGrievances;

    @Column(name = "resolve_rate")
    Float resolveRate;

    @Column(name = "rate_of_appealed_grievance")
    Float rateOfAppealedGrievance;

    @Column(name = "total_rating")
    Long totalRating;

    @Column(name = "average_rating")
    Float averageRating;

    @Column(name = "appeal_total")
    Long appealTotal;

    @Column(name = "appeal_current_month_acceptance")
    Long appealCurrentMonthAcceptance;

    @Column(name = "appeal_ascertain")
    Long appealAscertain;

    @Column(name = "appeal_running")
    Long appealRunning;

    @Column(name = "appeal_time_expired")
    Long appealTimeExpired;

    @Column(name = "appeal_resolved")
    Long appealResolved;

    @Column(name = "appeal_resolve_rate")
    Float appealResolveRate;

    @Column(name = "sub_offices_total_grievance")
    Long subOfficesTotalGrievance;

    @Column(name = "sub_offices_time_expired_grievance")
    Long subOfficesTimeExpiredGrievance;

    @Column(name = "sub_offices_resolved_grievance")
    Long subOfficesResolvedGrievance;

    @Column(name = "sub_offices_total_appeal")
    Long subOfficesTotalAppeal;

    @Column(name = "sub_offices_time_expired_appeal")
    Long subOfficesTimeExpiredAppeal;

    @Column(name = "sub_offices_resolved_appeal")
    Long subOfficesResolvedAppeal;

    @Column(name = "sub_offices_grievance_resolve_rate")
    Float subOfficesGrievanceResolveRate;

    @Column(name = "sub_offices_appeal_resolve_rate")
    Float subOfficesAppealResolveRate;
}
