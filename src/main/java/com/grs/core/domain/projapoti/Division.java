package com.grs.core.domain.projapoti;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Acer on 03-Oct-17.
 */
@Entity
@Data
@Table( name = "geo_divisions" )
public class Division {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "division_name_eng")
    private String nameEnglish;

    @Column(name = "division_name_bng")
    private String nameBangla;

}
