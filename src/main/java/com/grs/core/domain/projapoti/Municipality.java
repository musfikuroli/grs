package com.grs.core.domain.projapoti;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Acer on 17-Oct-17.
 */
@Data
@Entity
@Table(name = "geo_municipalities")
public class Municipality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column( name = "geo_division_id")
    private Integer divisionId;

    @Column(name = "geo_district_id")
    private Integer districtId;

    @Column(name = "municipality_name_eng")
    private  String nameEnglish;

    @Column(name = "municipality_name_bng")
    private  String nameBangla;
}
