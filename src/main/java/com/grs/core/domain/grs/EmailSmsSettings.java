package com.grs.core.domain.grs;

import com.grs.core.domain.ContactMedium;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by HP on 3/13/2018.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_sms_settings")
public class EmailSmsSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type")
    private ContactMedium type;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private Long port;

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "url")
    private String url;

    @Column(name = "ms_prefix")
    private String ms_prefix;

    @Column(name = "disabled")
    private Boolean disabled = false;
}
