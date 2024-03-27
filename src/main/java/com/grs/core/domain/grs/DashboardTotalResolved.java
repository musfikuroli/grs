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
@Table(name = "grs_dashboard_total_resolved")
public class DashboardTotalResolved implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "office_id")
    Long officeId;

    @Column(name = "office_name")
    String officeName;

    @Column(name = "total_count")
    Long totalCount;

    @Column(name = "resolved_count")
    Long resolvedCount;

    @Column(name = "expired_count")
    Long expiredCount;

    @Column(name = "rate")
    Double rate;
}
