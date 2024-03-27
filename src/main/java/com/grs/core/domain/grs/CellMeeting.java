package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import com.grs.core.domain.grs.Grievance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 14-Mar-18.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cell_meetings")
public class CellMeeting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "meeting_date")
    private Date meetingDate;

    @Column(name = "subject")
    private String subject;

    @Column(name = "meeting_number")
    private String meetingNumber;

    @Column(name = "note")
    private String note;

    @NotFound(action = NotFoundAction.IGNORE)
    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "cell_meetings_to_complaints",
            joinColumns = @JoinColumn(name = "cell_meeting_id", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "complaint_id", referencedColumnName = "ID"))
    private List<Grievance> grievances;
}
