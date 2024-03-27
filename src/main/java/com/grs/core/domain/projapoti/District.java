package com.grs.core.domain.projapoti;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Acer on 02-Oct-17.
 */
@Entity
@Data
@Table(name = "geo_districts")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "geo_division_id")
    private Integer divisionId;

    @Column(name = "district_name_eng")
    private String nameEnglish;

    @Column(name = "district_name_bng")
    private String nameBangla;
}
