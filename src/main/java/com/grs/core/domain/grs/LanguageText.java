package com.grs.core.domain.grs;

import com.grs.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by Acer on 9/9/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "language_text")
public class LanguageText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "menuID")
    private Long menuId;

    @Column(name = "languageTextEnglish")
    private String languageTextEnglish;

    @Column(name = "languageTextBangla")
    private String languageTextBangla;

    @Column(name = "languageConstantPrefix")
    private String languageConstantPrefix;

    @Column(name = "languageConstant")
    private String languageConstant;
}
