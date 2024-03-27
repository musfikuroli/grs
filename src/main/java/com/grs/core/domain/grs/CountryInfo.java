package com.grs.core.domain.grs;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Acer on 02-Jan-18.
 */
@Data
@Entity
@Table(name = "nationalities")
public class CountryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column( name = "country_name_eng")
    private String countryNameEng;

    @Column(name = "nationality_eng")
    private String nationalityEng;

    @Column( name = "country_name_bng")
    private String countryNameBng;

    @Column(name = "nationality_bng")
    private String nationalityBng;
}
