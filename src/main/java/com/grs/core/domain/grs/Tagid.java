package com.grs.core.domain.grs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tagid")
public class Tagid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "office_name")
    private String officeName;

    @Column(name = "office_unit_organogram_id")
    private Long officeUnitOrganogramId;

    @Column(name = "complaint_id")
    private Long complaintId;

    @Column(name = "complaint_office_id")
    private Long complaintOfficeId;

    @Column(name = "giving_date")
    private Date givingDate;

    @Column(name = "note")
    private String note;
}
