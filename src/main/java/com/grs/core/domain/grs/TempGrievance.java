package com.grs.core.domain.grs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "temp_complaints")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TempGrievance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "user")
    String user;

    @Column(name = "password")
    String password;

    @Column(name = "name")
    String name;

    @Column(name = "email")
    String email;

    @Column(name = "subject")
    String subject;

    @Column(name = "body")
    String body;

    @Column(name = "office_id")
    String officeId;

    @Column(name = "complainant_phone_number")
    String complainantPhoneNumber;

    @Column(name = "grs_tracking_no")
    String grsTrackingNo;

    @Column(name = "mygov_tracking_no")
    String mygovTrackingNo;

    @Column(name = "status")
    String status;

    @Column(name = "created_at")
    Date createdAt;

    @Column(name = "updatedAt")
    Date updatedAt;
}
