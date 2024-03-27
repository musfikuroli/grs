package com.grs.core.domain.grs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sp_programme")
public class SpProgramme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name_en", unique = true)
    private String nameEn;

    @Column(name = "name_bn", unique = true)
    private String nameBn;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "status")
    private Boolean status;
}