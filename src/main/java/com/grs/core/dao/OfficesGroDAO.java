package com.grs.core.dao;

import com.grs.api.model.response.OfficesGroDTO;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.grs.OfficesGRORepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 08-Oct-17.
 */
@Slf4j
@Service
public class OfficesGroDAO {
    @Autowired
    private OfficeDAO officeDAO;
    @Autowired
    private OfficesGRORepo officesGRORepo;

    public List<BigInteger> findOfficeIds() {
        return this.officesGRORepo.findOfficeIds();
    }

    public OfficesGRO findOfficesGROByOfficeId(Long id) {
        return this.officesGRORepo.findByOfficeId(id);
    }

    public List<OfficesGRO> findOfficesGroByMissingOfficerType(
            Long missingOfficerType,
            Long officeLayers,
            Long firstSelection,
            Long secondSelection
    ) {
        Specification specification = this.getListViewSpecification(
                missingOfficerType,
                officeLayers,
                firstSelection,
                secondSelection
        );

        return this.officesGRORepo.findAll(specification);
    }

    public List<OfficesGRO> findOfficesGROByMissingGRO() {
        return this.officesGRORepo.findByGroOfficeUnitOrganogramIdIsNull();
    }

    public List<OfficesGRO> findOfficesGROByMissingAO() {
        return this.officesGRORepo.findByAppealOfficerOfficeUnitOrganogramIdIsNull();
    }

    public List<OfficesGRO> findOfficesGROByMissingAdmin() {
        return this.officesGRORepo.findByAdminOfficeUnitOrganogramIdIsNull();
    }

    public List<OfficesGRO> findByAppealOfficeUnitOrganogramId(Long officeUnitOrganogramId){
        return this.officesGRORepo.findByAppealOfficerOfficeUnitOrganogramId(officeUnitOrganogramId);
    }

    public List<OfficesGRO> findByAdminOfficeUnitOrganogramId(Long officeUnitOrganogramId){
        return this.officesGRORepo.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);
    }

    public OfficesGRO save(OfficesGRO officesGRO) {
        return officesGRORepo.save(officesGRO);
    }

    public OfficesGroDTO convertToOfficesGroDTO(OfficesGRO officesGRO) {
        if(officesGRO == null || officesGRO.getOfficeId() == null) {
            return null;
        }
        Office office = this.officeDAO.findOne(officesGRO.getOfficeId());
        return OfficesGroDTO.builder()
                .id(officesGRO.getId())
                .officeId(officesGRO.getOfficeId())
                .officeName(office != null ? office.getNameBangla() : null)
                .groOfficeId(officesGRO.getGroOfficeId())
                .groOfficeUnitOrganogramId(officesGRO.getGroOfficeUnitOrganogramId())
                .appealOfficeId(officesGRO.getAppealOfficeId())
                .appealOfficerOfficeUnitOrganogramId(officesGRO.getAppealOfficerOfficeUnitOrganogramId())
                .adminOfficeId(officesGRO.getAdminOfficeId())
                .adminOfficeUnitOrganogramId(officesGRO.getAdminOfficeUnitOrganogramId())
                .isAppealOfficer(officesGRO.getIsAppealOfficer())
                .build();
    }

    public List<OfficesGRO> findAll() {
        return this.officesGRORepo.findAll();
    }

    public List<Long> getGRSEnabledOfficeIdFromOfficeIdList(List<Long> officeIdList) {
        List<BigInteger> resultList = officesGRORepo.findGRSEnabledOfficeIdIn(officeIdList);
        return resultList.stream()
                .map(BigInteger::longValue)
                .collect(Collectors.toList());
    }

    public List<OfficesGRO> findByAppealOfficeAndAppealOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId) {
        return this.officesGRORepo.findByAppealOfficeIdAndAppealOfficerOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
    }

    public Page<OfficesGRO> findAllOffices(Pageable pageable) {
        return this.officesGRORepo.findAll(pageable);
    }

    public OfficesGRO findOne(Long id) {
        return this.officesGRORepo.findOne(id);
    }

    public Page<OfficesGRO> findActiveOffices(Pageable pageable) {
        List<OfficesGRO> setupOffices = new ArrayList<>();
        List<OfficesGRO> officesGROes = this.findAll();
        officesGROes.forEach(officesGRO -> {
            if(officesGRO.getGroOfficeId()!= null && officesGRO.getGroOfficeUnitOrganogramId()!=null && officesGRO.getAppealOfficeId()!=null && officesGRO.getAppealOfficerOfficeUnitOrganogramId()!=null){
                setupOffices.add(officesGRO);
            }
        });
        Page<OfficesGRO> offices = new PageImpl<OfficesGRO>(setupOffices);
        return offices;
    }

    public List<OfficesGRO> findActiveOffices() {
        List<OfficesGRO> setupOffices = new ArrayList<>();
        List<OfficesGRO> officesGROes = this.findAll();
        officesGROes.forEach(officesGRO -> {
            if(officesGRO.getGroOfficeId()!= null && officesGRO.getGroOfficeUnitOrganogramId()!=null && officesGRO.getAppealOfficeId()!=null && officesGRO.getAppealOfficerOfficeUnitOrganogramId()!=null){
                setupOffices.add(officesGRO);
            }
        });
//        List<OfficesGRO> offices = new PageImpl<OfficesGRO>(setupOffices);
        return setupOffices;
    }

    public OfficesGRO findByOfficeId(Long officeId) {
        return this.officesGRORepo.findByOfficeId(officeId);
    }

    public Specification<OfficesGRO> getListViewSpecification(
            Long missingOfficerType,
            Long officeLayers,
            Long firstSelection,
            Long secondSelection
    ) {

        Specification<OfficesGRO> specification = new Specification<OfficesGRO>() {
            public Predicate toPredicate(Root<OfficesGRO> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> predicates = new ArrayList<Predicate>();

                if (missingOfficerType == 1L) predicates.add(builder.isNull(root.get("groOfficeUnitOrganogramId")));
                if (missingOfficerType == 2L) predicates.add(builder.isNull(root.get("appealOfficerOfficeUnitOrganogramId")));
                if (missingOfficerType == 3L) predicates.add(builder.isNull(root.get("adminOfficeUnitOrganogramId")));

                if (secondSelection != null) predicates.add(builder.equal(root.get("officeId"), secondSelection));
                else if (officeLayers != null) {

                    if (officeLayers != 3) {
                        predicates.add(builder.equal(root.get("layerLevel"), officeLayers));
                    }
                    predicates.add(builder.equal(root.get("customLayerLevel"), officeLayers));

                    if (firstSelection != null) {

                        if (officeLayers == 0 || officeLayers == 1 || officeLayers == 2) {

                        }
                        else if (officeLayers == 3) {
                            predicates.add(builder.equal(root.get("customLayerId"), firstSelection));
                        }
                        else {
                            predicates.add(builder.equal(root.get("officeOriginId"), firstSelection));
                        }

                    }

                }

                query.orderBy(builder.desc(root.get("officeNameEnglish")));

                return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }
}
