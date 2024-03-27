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
@Table(name = "safety_net_grievance")
public class SafetyNetGrievance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "grievance_id")
    Long grievanceId;

    @Column(name = "safety_net_id")
    Integer safetyNetId;

    @Column(name = "division_id")
    Integer divisionId;

    @Column(name = "district_id")
    Integer districtId;

    @Column(name = "upazila_id")
    Integer upazilaId;

    @Column(name = "status")
    boolean status;

    @Column(name = "sub_type")
    String subType;
}
