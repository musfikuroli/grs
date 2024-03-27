package com.grs.core.domain.projapoti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by Acer on 8/30/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "office_layers")
public class OfficeLayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(length = 255, name = "layer_name_bng")
    private String layerNameBangla;

    @Column(length = 255, name = "layer_name_eng")
    private String layerNameEnglish;

    @Column(name = "layer_level")
    private Integer layerLevel;

    @Column(name = "custom_layer_id")
    private Integer customLayerId;

    @Column(name = "layer_sequence")
    private Integer layerSequence;

    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "office_ministry_id", referencedColumnName = "id")
    private OfficeMinistry officeMinistry;
}
