package com.grs.core.domain.grs;

import com.grs.core.domain.LanguageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by HP on 2/6/2018.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_template")
public class EmailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_to_role_id", referencedColumnName = "ID")
    private ActionToRole actionToRole;

    @Column(name = "email_template_name")
    private String emailTemplateName;

    @Column(name = "email_subject_eng")
    private String emailTemplateSubjectEng;

    @Column(name = "email_subject_bng")
    private String emailTemplateSubjectBng;

    @Column(name = "email_body_eng")
    private String emailTemplateBodyEng;

    @Column(name = "email_body_bng")
    private String emailTemplateBodyBng;

    @Column(name = "status")
    private Boolean status = true;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "language")
    private LanguageStatus language;

}