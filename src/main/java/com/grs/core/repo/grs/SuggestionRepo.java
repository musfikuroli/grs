package com.grs.core.repo.grs;

import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.grs.Suggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Acer on 9/14/2017.
 */
@Repository
public interface SuggestionRepo extends JpaRepository<Suggestion, Long> {
    public Page<Suggestion> findByOfficeIdOrderByCreatedAtDesc(Long officeId, Pageable pageable);
}
