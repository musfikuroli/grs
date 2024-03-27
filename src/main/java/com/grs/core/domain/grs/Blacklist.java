package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by Acer on 9/9/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "blacklists")
public class Blacklist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "complainant_id")
    private Long complainantId;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "requested")
    private Boolean requested;

    @Column(name = "blacklisted")
    private Boolean blacklisted;

    @Column(name = "office_name")
    private String officeName;

    @Column(name = "reason")
    private String reason;
}
