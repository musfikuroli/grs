package com.grs.core.domain.doptor;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String user_alias;
    private String hash_change_password;
    private Integer user_role_id;
    private Integer is_admin;
    private boolean active;
    private String user_status;
    private Integer is_email_verified;
    private String email_verify_code;
    private Date verification_date;
    private String ssn;
    private boolean force_password_change;
    private Date last_password_change;
    private Date created;
    private Date modified;
    private String created_by;
    private String modified_by;
    private String photo;
    private Integer employee_record_id;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", user_alias='" + user_alias + '\'' +
                ", hash_change_password='" + hash_change_password + '\'' +
                ", user_role_id=" + user_role_id +
                ", is_admin=" + is_admin +
                ", active=" + active +
                ", user_status='" + user_status + '\'' +
                ", is_email_verified=" + is_email_verified +
                ", email_verify_code='" + email_verify_code + '\'' +
                ", verification_date=" + verification_date +
                ", ssn='" + ssn + '\'' +
                ", force_password_change=" + force_password_change +
                ", last_password_change=" + last_password_change +
                ", created=" + created +
                ", modified=" + modified +
                ", created_by='" + created_by + '\'' +
                ", modified_by='" + modified_by + '\'' +
                ", photo='" + photo + '\'' +
                ", employee_record_id=" + employee_record_id +
                '}';
    }
}
