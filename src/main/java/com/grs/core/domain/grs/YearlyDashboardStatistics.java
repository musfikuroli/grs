package com.grs.core.domain.grs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "grs_current_year_statistics")
public class YearlyDashboardStatistics implements Serializable {

    @Id
    @Column(name = "id")
    Integer id;

    @Column(name = "total_complaint")
    Long totalComplaint;

    @Column(name = "total_forwarded")
    Long totalForwarded;

    @Column(name = "total_resolved")
    Long totalResolved;
}
