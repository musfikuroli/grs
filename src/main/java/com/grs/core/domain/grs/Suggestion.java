package com.grs.core.domain.grs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grs.core.domain.*;
import com.grs.core.domain.projapoti.Office;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by User on 10/2/2017.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "suggestions")
public class Suggestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "office_id")
    private Long officeId;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "suggestion")
    private String suggestion;

    @Column(name = "description")
    private String description;

    @Column(name = "office_service_name")
    private String officeServiceName;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type_of_opinion")
    private ImprovementSuggestion typeOfSuggestionForImprovement;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "probable_improvement")
    private EffectsTowardsSolution effectTowardsSolution;

    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_service_id", referencedColumnName = "id")
    private CitizenCharter citizenCharter;
}
