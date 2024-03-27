package com.grs.core.domain.projapoti;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by Acer on 9/6/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_units")
public class OfficeUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", referencedColumnName = "id")
    private Office office;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_ministry_id", referencedColumnName = "id")
    private OfficeMinistry officeMinistry;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_layer_id", referencedColumnName = "id")
    private OfficeLayer officeLayer;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_unit_id", referencedColumnName = "id")
    private OfficeUnit parentOfficeUnit;

    @Column(name = "unit_name_eng")
    private String unitNameEnglish;

    @Column(name = "unit_name_bng")
    private String unitNameBangla;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phoneNumber;
}

