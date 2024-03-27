package com.grs.core.dao;

import com.grs.api.model.UserInformation;
import com.grs.api.model.request.BlacklistRequestBodyDTO;
import com.grs.core.domain.grs.Blacklist;
import com.grs.core.repo.grs.BlacklistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlacklistDAO {

    @Autowired
    private BlacklistRepo blacklistRepo;

    public Blacklist findById(Long id){
        return blacklistRepo.findOne(id);
    }

    public List<Blacklist> findByComplainantId(Long id){
        return blacklistRepo.findByComplainantId(id);
    }

    public Blacklist save(Blacklist blacklist){
        return blacklistRepo.save(blacklist);
    }

    public boolean doBlacklistRequestByComplainantId(Long complainantId, Long officeId){
        Blacklist blacklist = this.blacklistRepo.findByComplainantIdAndOfficeId(complainantId, officeId);
        blacklist.setRequested(true);
        return this.save(blacklist) != null;
    }

    public boolean doBlacklistRequestByComplainantId(BlacklistRequestBodyDTO blacklistRequestBodyDTO, UserInformation userInformation) {
        Long complainantId = blacklistRequestBodyDTO.getComplainantId();
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Blacklist blacklist = this.blacklistRepo.findByComplainantIdAndOfficeId(complainantId, officeId);
        if (blacklist != null) {
            return false;
        }
        blacklist = Blacklist.builder()
                .requested(true)
                .blacklisted(false)
                .complainantId(complainantId)
                .officeId(officeId)
                .officeName(userInformation.getOfficeInformation().getOfficeNameBangla())
                .reason(blacklistRequestBodyDTO.getBlacklistReason())
                .build();

        return this.save(blacklist) != null;
    }

    public boolean doBlacklistByComplainantId(Long complainantId, Long officeId) {
        Blacklist blacklist = this.blacklistRepo.findByComplainantIdAndOfficeId(complainantId, officeId);
//        if (complainant.getIsBlacklisted() != null && complainant.getIsBlacklisted().equals(true)) {
        if (blacklist != null && blacklist.getBlacklisted()) {
            return false;
        }
        blacklist.setBlacklisted(true);
//        complainant.setBlacklisterOfficeId(officeId);
        return this.save(blacklist) != null;
    }


    public Boolean isBlacklistedUserByComplainantId(Long complainantId) {
        List<Blacklist> blacklists = this.blacklistRepo.findByComplainantId(complainantId);
        if (blacklists.size() == 0){
            return false;
        }
        Boolean flag = false;
        for (Blacklist blacklist : blacklists){
            if(blacklist.getBlacklisted()){
                flag = true;
                break;
            }
        }
        return flag;
    }

    public List<Blacklist> getBlacklistByOfficeId(Long officeId) {
        return this.blacklistRepo.findByOfficeId(officeId);
    }

    public List<Blacklist> getBlacklistByChildOfficesAndIsRequested(List<Long> officeIds) {
        return this.blacklistRepo.findByOfficeIdInAndAndBlacklistedOrOfficeIdInAndRequested(officeIds, true, officeIds,  true);
    }

    public boolean doUnBlacklistByComplainantId(Long complainantId, Long officeId) {
        Blacklist blacklist = this.blacklistRepo.findByComplainantIdAndOfficeId(complainantId, officeId);
        if (blacklist != null && !blacklist.getBlacklisted()) {
            return false;
        }
        blacklist.setBlacklisted(false);
//        complainant.setBlacklisterOfficeId(null);
        return this.save(blacklist) != null;
    }

    public boolean doUnBlacklistRequestByComplainantId(Long complainantId, Long officeId) {
        Blacklist blacklist = this.blacklistRepo.findByComplainantIdAndOfficeId(complainantId, officeId);
        if (blacklist != null && blacklist.getRequested().equals(false)) {
            return false;
        }
        blacklist.setRequested(false);
//        complainant.setBlacklisterOfficeId(null);

        return this.save(blacklist) != null;
    }
}
