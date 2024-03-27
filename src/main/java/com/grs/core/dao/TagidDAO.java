package com.grs.core.dao;

import com.grs.core.domain.grs.Tagid;
import com.grs.core.repo.grs.TagidRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TagidDAO {
    @Autowired
    private TagidRepo tagidRepo;

    public Tagid save(Tagid tagid){
        return tagidRepo.save(tagid);
    }

    public Page<Tagid> findByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrgamogramId, Pageable pageable){
        return  tagidRepo.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrgamogramId, pageable);
    }
}
