package com.grs.core.domain.projapoti;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Acer on 02-Oct-17.
 */
@Entity
@Data
@Table(name = "geo_upazilas")
public class Upazila {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "geo_division_id")
    private Integer divisionId;

    @Column(name = "geo_district_id")
    private Integer districtId;

    @Column(name = "upazila_name_eng")
    private String nameEnglish;

    @Column(name = "upazila_name_bng")
    private String nameBangla;
}
