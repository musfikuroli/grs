package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Acer on 9/9/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "actions")
public class Action extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "action_eng")
    private String actionEng;

    @Column(name = "action_bng")
    private String actionBng;

    @Column(name = "link")
    private String link;

    @Column(name = "icon_link")
    private String iconLink;
}
