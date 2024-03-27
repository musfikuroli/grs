package com.grs.core.domain.grs;

import com.grs.core.domain.grs.EmailTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by HP on 3/11/2018.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "grs_role_to_email")
public class GrsRoleToEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_template_id", referencedColumnName = "ID")
    private EmailTemplate emailTemplate;

    @Column(name = "grs_role")
    private String grsRole;

    @Column(name = "status")
    private Boolean status = true;
}
