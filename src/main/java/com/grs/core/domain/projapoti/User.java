package com.grs.core.domain.projapoti;

/**
 * Created by Tanvir on 8/13/2017.
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by Tanvir on 4/9/2017.
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(length = 50, name = "username")
    private String username;

    @Column(length = 50, name = "password")
    private String password;

    @NotNull
    @Column(name = "is_email_verified", columnDefinition = "TINYINT(4)")
    private Boolean authenticated;

    @Column(length = 255, name = "email_verify_code")
    private String confirmationCode;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_record_id", referencedColumnName = "ID")
    private EmployeeRecord employeeRecord;

    @Column(name = "is_admin")
    private Boolean isAdmin = false;

}

