package com.grs.core.domain.grs;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Acer on 02-Jan-18.
 */
@Data
@Entity
@Table(name = "nationalities")
public class NationalityAndCountry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column( name = "country_name")
    private String country;

    @Column(name = "nationality")
    private String nationality;
}
