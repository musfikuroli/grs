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
@Table(name = "sms_template")
public class SmsTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_to_role_id", referencedColumnName = "ID")
    private ActionToRole actionToRole;

    @Column(name = "sms_template_name")
    private String smsTemplateName;

    @Column(name = "sms_body_eng")
    private String smsTemplateBodyEng;

    @Column(name= "sms_body_bng")
    private String smsTemplateBodyBng;

    @Column(name = "status")
    private Boolean status = true;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "language")
    private LanguageStatus language;
}
