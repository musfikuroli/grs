package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.List;

/**
 * Created by HP on 4/3/2018.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "citizens_charter_origin")
public class CitizensCharterOrigin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_origin_id")
    private Long officeOriginId;

    @Column(name = "layer_level")
    private Long layerLevel;

    @Column(name = "office_origin_name_bng")
    private String officeOriginNameBangla;

    @Column(name = "office_origin_name_eng")
    private String officeOriginNameEnglish;

    @Column(name = "vision_bng")
    private String visionBangla;

    @Column(name = "vision_eng")
    private String visionEnglish;

    @Column(name = "mission_bng")
    private String missionBangla;

    @Column(name = "mission_eng")
    private String missionEnglish;

    @Column(name = "expectations_bng")
    private String expectationBangla;

    @Column(name = "expectations_eng")
    private String expectationEnglish;
}
