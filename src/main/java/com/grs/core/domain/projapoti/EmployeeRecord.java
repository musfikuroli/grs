package com.grs.core.domain.projapoti;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;


import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 9/6/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employee_records")
public class EmployeeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name_eng")
    private String nameEnglish;

    @Column(name = "name_bng")
    private String nameBangla;

    @Column(name = "mother_name_eng")
    private String motherNameEnglish;

    @Column(name = "mother_name_bng")
    private String motherNameBangla;

    @Column(name = "father_name_eng")
    private String fatherNameEnglish;

    @Column(name = "father_name_bng")
    private String fatherNameBangla;

    @Column(name = "personal_email")
    private String personalEmail;

    @Column(name = "personal_mobile")
    private String personalMobile;

    @Column(name = "nid")
    private String nationalId;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "employee_offices",
            joinColumns = @JoinColumn(name = "employee_record_id", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "office_id", referencedColumnName = "ID"))
    private List<Office> offices;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "employee_offices",
            joinColumns = @JoinColumn(name = "employee_record_id", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "office_unit_id", referencedColumnName = "ID"))
    private List<OfficeUnit> officeUnits;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "employee_offices",
            joinColumns = @JoinColumn(name = "employee_record_id", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "office_unit_organogram_id", referencedColumnName = "ID"))
    private List<OfficeUnitOrganogram> officeUnitOrganograms;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToMany(mappedBy = "employeeRecord", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<EmployeeOffice> employeeOffices;
}


