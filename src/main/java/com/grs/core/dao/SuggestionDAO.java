package com.grs.core.dao;

import com.grs.api.model.request.SuggestionRequestDTO;
import com.grs.core.domain.*;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.Feedback;
import com.grs.core.domain.grs.Suggestion;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.grs.SuggestionRepo;
import com.grs.core.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by User on 10/3/2017.
 */
@Service
public class SuggestionDAO {
    @Autowired
    private SuggestionRepo suggestionRepo;

    public Suggestion findOne(Long id) {
        return this.suggestionRepo.findOne(id);
    }

    public Suggestion save(Suggestion suggestion) {
        return this.suggestionRepo.save(suggestion);
    }

    public Suggestion addSuggestion(SuggestionRequestDTO suggestionRequestDTO, CitizenCharter citizenCharter) {
        Suggestion suggestion = Suggestion.builder()
            .description(suggestionRequestDTO.getDescription())
            .name(suggestionRequestDTO.getName())
            .email(suggestionRequestDTO.getEmail())
            .phone(suggestionRequestDTO.getPhone())
            .officeId(suggestionRequestDTO.getOfficeId())
            .citizenCharter(citizenCharter)
            .officeServiceName(suggestionRequestDTO.getOfficeServiceName())
            .suggestion(suggestionRequestDTO.getSuggestion())
            .typeOfSuggestionForImprovement(suggestionRequestDTO.getTypeOfSuggestionForImprovement())
            .effectTowardsSolution(suggestionRequestDTO.getEffectTowardsSolution())
            .build();
        return this.save(suggestion);
    }

    public Page<Suggestion> getSuggestionByOfficeId(Long officeId, Pageable pageable){
        return this.suggestionRepo.findByOfficeIdOrderByCreatedAtDesc(officeId, pageable);
    }
}
