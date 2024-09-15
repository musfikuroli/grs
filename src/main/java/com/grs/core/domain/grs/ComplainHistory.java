package com.grs.core.domain.grs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "complain_history")
public class ComplainHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;
    @Column(name = "complain_id")
    Long complainId;
    @Column(name = "tracking_number")
    String trackingNumber;
    @Column(name = "current_status")
    String currentStatus;
    @Column(name = "office_id")
    Long officeId;
    @Column(name = "layer_level")
    Long layerLevel;
    @Column(name = "custom_layer")
    Long customLayer;
    @Column(name = "office_origin")
    Long officeOrigin;
    @Column(name = "medium_of_submission")
    String mediumOfSubmission;
    @Column(name = "grievance_type")
    String grievanceType;
    @Column(name = "self_motivated")
    Long selfMotivated;
    @Column(name = "created_at")
    Date createdAt;
    @Column(name = "closed_at")
    Date closedAt;

    @Column(name = "created_year_month_day")
    private String createdYearMonthDay;
}
