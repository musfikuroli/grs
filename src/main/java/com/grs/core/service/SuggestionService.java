package com.grs.core.service;

import com.grs.api.model.UserInformation;
import com.grs.api.model.request.SuggestionDetailsDTO;
import com.grs.api.model.request.SuggestionRequestDTO;
import com.grs.api.model.response.SuggestionResponseDTO;
import com.grs.core.dao.SuggestionDAO;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.Suggestion;
import com.grs.core.domain.projapoti.Office;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.Utility;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Created by User on 10/3/2017.
 */
@Slf4j
@Service
public class SuggestionService {
    @Autowired
    private OfficeService officeService;
    @Autowired
    private SuggestionDAO suggestionDAO;
    @Autowired
    private CitizenCharterService citizenCharterService;
    @Autowired
    private MessageService messageService;

    public Suggestion addSuggestion(SuggestionRequestDTO suggestionRequestDTO) {
        Long officeServiceId = suggestionRequestDTO.getOfficeServiceId();
        if(officeServiceId == null) {
            officeServiceId = 0L;
        }
        CitizenCharter citizenCharter = citizenCharterService.findOne(officeServiceId);
        return this.suggestionDAO.addSuggestion(suggestionRequestDTO, citizenCharter);
    }

    public Page<SuggestionResponseDTO> getSuggestionByOfficeId(Long officeId, Pageable pageable) {
        Page<Suggestion> suggestions = this.suggestionDAO.getSuggestionByOfficeId(officeId, pageable);
        return suggestions.map(this::convertToSuggestionResponseDTO);
    }

    public SuggestionResponseDTO convertToSuggestionResponseDTO(Suggestion suggestion) {
        CitizenCharter citizenCharter = suggestion.getCitizenCharter();
        String serviceName = citizenCharter == null ? suggestion.getOfficeServiceName() : (messageService.getCurrentLanguageCode().equals("en") ? citizenCharter.getServiceNameEnglish() : citizenCharter.getServiceNameBangla());
        Office office = officeService.getOffice(suggestion.getOfficeId());
        return SuggestionResponseDTO.builder()
                .id(suggestion.getId())
                .officeName(office.getNameBangla())
                .serviceName(serviceName)
                .subject(BanglaConverter.convertImprovementSuggestionTypeToBangla(suggestion.getTypeOfSuggestionForImprovement()))
                .description(suggestion.getDescription())
                .suggestion(suggestion.getSuggestion())
                .possibleEffect(BanglaConverter.convertEffectTypeToBangla(suggestion.getEffectTowardsSolution()))
                .submissionDate(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(suggestion.getCreatedAt())))
                .build();
    }

    public SuggestionDetailsDTO getSuggestionDetails(Long id) {
        Suggestion suggestion = this.suggestionDAO.findOne(id);
        CitizenCharter citizenCharter = suggestion.getCitizenCharter();
        return SuggestionDetailsDTO.builder()
                .description(suggestion.getDescription())
                .name(suggestion.getName())
                .email(suggestion.getEmail())
                .phone(suggestion.getPhone())
                .officeName(this.officeService.getOffice(suggestion.getOfficeId()).getNameBangla())
                .officeServiceName(citizenCharter == null ? suggestion.getOfficeServiceName() : (messageService.getCurrentLanguageCode().equals("en") ? citizenCharter.getServiceNameEnglish() : citizenCharter.getServiceNameBangla()))
                .suggestion(suggestion.getSuggestion())
                .typeOfSuggestionForImprovement(suggestion.getTypeOfSuggestionForImprovement() == null ? null : BanglaConverter.convertImprovementSuggestionTypeToBangla(suggestion.getTypeOfSuggestionForImprovement()))
                .effectTowardsSolution(suggestion.getEffectTowardsSolution() == null ? null : BanglaConverter.convertEffectTypeToBangla(suggestion.getEffectTowardsSolution()))
                .submissionDate(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(suggestion.getCreatedAt())))
                .build();
    }
}
