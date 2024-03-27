package com.grs.core.service;

import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.response.organogram.*;
import com.grs.core.dao.OfficeDAO;
import com.grs.core.dao.OfficeOriginUnitDAO;
import com.grs.core.dao.OfficeUnitDAO;
import com.grs.core.dao.OfficeUnitOrganogramDAO;
import com.grs.core.domain.projapoti.EmployeeOffice;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeOriginUnit;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import com.grs.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 10/4/2017.
 */
@Service
public class OfficeOrganogramService {
    @Autowired
    private OfficeDAO officeDAO;
    @Autowired
    private OfficeUnitDAO officeUnitDAO;
    @Autowired
    private OfficeUnitOrganogramDAO officeUnitOrganogramDAO;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private OfficeMinistryService officeMinistryService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private OfficeOriginUnitDAO officeOriginUnitDAO;

    public OfficeUnitOrganogram findOne(Long id) {
        return this.officeUnitOrganogramDAO.findOne(id);
    }

    public OfficeUnitOrganogram findOfficeUnitOrganogramById(Long id) {
        return this.officeUnitOrganogramDAO.findOfficeUnitOrganogramById(id);
    }

    public List<TreeNodeDTO> getSubOfficesWithOrganograms(String id, Authentication authentication) {
        String nodeId = id;
        String officeMinistryId;
        String officeId;
        List<TreeNodeDTO> listTreeNodeDTO = new ArrayList<>();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return null;
        }
        Long employeeMinistryId = userInformation.getOfficeInformation().getOfficeMinistryId();
        Long employeeOfficeId = userInformation.getOfficeInformation().getOfficeId();
        if (nodeId.equals("#")) {
            listTreeNodeDTO = this.getDescendentOfficesWithUnits(nodeId, authentication);
        } else {
            TreeNodeDTO treeNodeDTO = new TreeNodeDTO();
            treeNodeDTO.setId("");
            treeNodeDTO.setIcon("fa fa-ban");
            treeNodeDTO.setChildren(false);
            treeNodeDTO.setText("<i>তথ্য নেই</i>");
            listTreeNodeDTO.add(treeNodeDTO);
        }
        return listTreeNodeDTO;
    }

    public List<TreeNodeDTO> getOrganogram(String id, Authentication authentication) {
        String nodeId = id;
        String officeMinistryId;
        String officeId;
        List<TreeNodeDTO> listTreeNodeDTO = new ArrayList<>();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return null;
        }
        Long employeeMinistryId = userInformation.getOfficeInformation().getOfficeMinistryId();
        Long employeeOfficeId = userInformation.getOfficeInformation().getOfficeId();
        if (nodeId.equals("#")) {
            TreeNodeDTO node = new TreeNodeDTO();
            node.setId("units_" + employeeMinistryId + "_" + employeeOfficeId + "_0_root");
            node.setDataId("units_" + employeeMinistryId + "_" + employeeOfficeId + "_0_root");
            node.setText("শাখাসমূহ");
            node.setIcon("fa fa-caret-square-o-right");
            node.setChildren(true);
            listTreeNodeDTO.add(node);

        } else {
            String[] splittedNodeId = nodeId.split("_");
            String type = splittedNodeId[0];
            officeMinistryId = splittedNodeId[1];
            officeId = splittedNodeId[2];
            long parentOfficeUnitId = Long.parseLong(splittedNodeId[3]);
            String nodeExt = "";
            if (splittedNodeId.length == 5) {
                nodeExt = splittedNodeId[4];
            }

            if (nodeExt.equals("root")) {
                List<OfficeUnitDTO> listOfficeUnitsDTO = officeUnitDAO.getDTOsByOfficeMinistryIDAndOfficeIdAndParentUnitId(Long.parseLong(officeMinistryId), Long.parseLong(officeId), parentOfficeUnitId);
                if (listOfficeUnitsDTO.size() > 0) {
                    for (OfficeUnitDTO officeUnitsDTO : listOfficeUnitsDTO) {
                        if (officeUnitsDTO.getParentUnitId().equals(parentOfficeUnitId)) {
                            TreeNodeDTO node = new TreeNodeDTO();
                            node.setId("units_" + officeMinistryId + "_" + officeId + "_" + officeUnitsDTO.getId());
                            node.setDataId("units_" + officeMinistryId + "_" + officeId + "_" + officeUnitsDTO.getId());
                            node.setText("<span style='color: blue'>" + officeUnitsDTO.getUnitNameBng() + "</span>");
                            node.setIcon("fa fa-caret-square-o-right");
                            node.setChildren(true);
                            listTreeNodeDTO.add(node);
                        }
                    }
                } else {
                    TreeNodeDTO treeNodeDTO = new TreeNodeDTO();
                    treeNodeDTO.setId("");
                    treeNodeDTO.setIcon("fa fa-ban");
                    treeNodeDTO.setChildren(false);
                    treeNodeDTO.setText("<i>তথ্য নেই</i>");
                    listTreeNodeDTO.add(treeNodeDTO);
                }
            } else if (nodeExt.equals("post")) {
                List<OfficeUnitOrganogramDTO> listOfficeUnitOrganogramsDTO = officeUnitOrganogramDAO.getPostsListByUnit(parentOfficeUnitId);
                if (listOfficeUnitOrganogramsDTO.size() > 0) {
                    for (OfficeUnitOrganogramDTO officeUnitOrganogramsDTO : listOfficeUnitOrganogramsDTO) {

                        EmployeeOffice employeeOffice = this.officeService
                                .findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(
                                        officeUnitOrganogramsDTO.getOfficeId(),
                                        officeUnitOrganogramsDTO.getId(), true);
                        TreeNodeDTO node = new TreeNodeDTO();
                        if (employeeOffice != null) {
                            String name = employeeOffice.getEmployeeRecord().getNameBangla();
                            node.setName(name);
                        }
                        node.setId("post_" + officeMinistryId + "_" + officeId + "_" + officeUnitOrganogramsDTO.getId());
                        node.setDataId("post_" + officeMinistryId + "_" + officeId + "_" + officeUnitOrganogramsDTO.getId());
                        node.setText("<span style='color: blue'>" + officeUnitOrganogramsDTO.getDesignationBng() + "</span>");
                        node.setIcon("fa fa-user");
                        node.setChildren(false);
                        listTreeNodeDTO.add(node);
                    }
                } else {
                    TreeNodeDTO treeNodeDTO = new TreeNodeDTO();
                    treeNodeDTO.setId("");
                    treeNodeDTO.setIcon("fa fa-ban");
                    treeNodeDTO.setChildren(false);
                    treeNodeDTO.setText("<i>তথ্য নেই</i>");
                    listTreeNodeDTO.add(treeNodeDTO);
                }
            } else {
                TreeNodeDTO node = new TreeNodeDTO();
                node.setId("units_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId + "_root");
                node.setDataId("units_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId + "_root");
                node.setText("শাখাসমূহ");
                node.setIcon("fa fa-caret-square-o-right");

                List<OfficeUnitDTO> listChildOfficeUnitsDTO = officeUnitDAO.getDTOsByOfficeMinistryIDAndOfficeIdAndParentUnitId(Long.parseLong(officeMinistryId), Long.parseLong(officeId), parentOfficeUnitId);
                if (listChildOfficeUnitsDTO.size() > 0) {
                    node.setChildren(true);
                } else {
                    node.setChildren(false);
                }
                listTreeNodeDTO.add(node);
                TreeNodeDTO node2 = new TreeNodeDTO();
                node2.setId("posts_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId + "_post");
                node2.setDataId("posts_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId + "_post");
                node2.setText("পদসমূহ");
                node2.setIcon("fa fa-users");
                List<OfficeUnitOrganogramDTO> listChildOfficeUnitOrganogramsDTO = officeUnitOrganogramDAO.getPostsListByUnit(parentOfficeUnitId);
                if (listChildOfficeUnitOrganogramsDTO.size() > 0) {
                    node2.setChildren(true);
                } else {
                    node2.setChildren(false);
                }
                listTreeNodeDTO.add(node2);
            }
        }
        return listTreeNodeDTO;
    }

    public List<TreeNodeDTO> getDescendentOffices(String id, Authentication authentication) {
        String nodeId = id;
        String officeMinistryId;
        String parentOfficeId;
        List<TreeNodeDTO> listTreeNodeDTO = new ArrayList<>();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return null;
        }
        Long employeeMinistryId = userInformation.getOfficeInformation().getOfficeMinistryId();
        if (nodeId.equals("#")) {
            List<Office> listOfficesDTO = officesGroService.findByAppealOfficer(
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId()
            );
            if (listOfficesDTO.size() > 0) {
                for (Office officesDTO : listOfficesDTO) {
                    TreeNodeDTO treeNodeDTO = TreeNodeDTO.builder()
                            .id("office_" + employeeMinistryId + "_" + officesDTO.getId())
                            .icon("fa fa-bank")
                            .text("<span style='color: blue'>" + officesDTO.getNameBangla() + "</span>")
                            .build();

                    if (officesDTO.getOfficeLayer() == null) {
                        treeNodeDTO.setChildren(false);
                    } else if (officesDTO.getOfficeLayer().getLayerLevel() == null) {
                        treeNodeDTO.setChildren(false);
                    } else if (officesDTO.getOfficeLayer().getLayerLevel() >= 5) {
                        treeNodeDTO.setChildren(false);
                    } else {
                        List<Office> listChildOfficesDTO = officesGroService.findByAppealOfficer(
                                userInformation.getOfficeInformation().getOfficeId(),
                                userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
                        if (listChildOfficesDTO.size() > 0) {
                            treeNodeDTO.setChildren(true);
                        } else {
                            treeNodeDTO.setChildren(false);
                        }
                    }
                    listTreeNodeDTO.add(treeNodeDTO);
                }
            } else {
                TreeNodeDTO treeNodeDTO = TreeNodeDTO.builder()
                        .id("")
                        .icon("fa fa-bank")
                        .children(false)
                        .text("<i>তথ্য নেই</i>")
                        .build();

                listTreeNodeDTO.add(treeNodeDTO);
            }
        } else {
            String[] splittedNodeId = nodeId.split("_");
            String type = splittedNodeId[0];
            officeMinistryId = splittedNodeId[1];
            parentOfficeId = splittedNodeId[2];
            List<Office> listOfficesDTO = officesGroService.findByAppealOfficer(
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
            if (listOfficesDTO.size() > 0) {
                for (Office officesDTO : listOfficesDTO) {
                    if (officesDTO.getParentOfficeId() == Long.parseLong(parentOfficeId)) {
                        TreeNodeDTO node = TreeNodeDTO.builder()
                                .id("office_" + officeMinistryId + "_" + officesDTO.getId())
                                .dataId("office_" + officeMinistryId + "_" + officesDTO.getId())
                                .text("<span style='color: blue'>" + officesDTO.getNameBangla() + "</span>")
                                .icon("fa fa-caret-square-o-right")
                                .build();

                        if (officesDTO.getOfficeLayer().getLayerLevel() >= 5) {
                            node.setChildren(false);
                        } else {
                            List<Office> listChildOfficesDTO = officesGroService.findByAppealOfficer(
                                    userInformation.getOfficeInformation().getOfficeId(),
                                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId());
                            if (listChildOfficesDTO.size() > 0) {
                                node.setChildren(true);
                            } else {
                                node.setChildren(false);
                            }
                        }
                        listTreeNodeDTO.add(node);
                    }
                }
            } else {
                TreeNodeDTO treeNodeDTO = TreeNodeDTO.builder()
                        .id("")
                        .icon("fa fa-ban")
                        .children(false)
                        .text("<i>তথ্য নেই</i>")
                        .build();

                listTreeNodeDTO.add(treeNodeDTO);
            }
        }
        return listTreeNodeDTO;
    }

    public List<TreeNodeDTO> getDescendentOfficesWithUnits(String id, Authentication authentication) {
        String nodeId = id;
        String officeMinistryId;
        String parentOfficeId;
        List<TreeNodeDTO> listTreeNodeDTO = new ArrayList<>();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return null;
        }
        Long employeeMinistryId = userInformation.getOfficeInformation().getOfficeMinistryId();
        if (nodeId.equals("#")) {
            List<Office> listOfficesDTO = officesGroService.findByAppealOfficer(
                    userInformation.getOfficeInformation().getOfficeId(),
                    userInformation.getOfficeInformation().getOfficeUnitOrganogramId()
            );
            if (listOfficesDTO.size() > 0) {
                for (Office officesDTO : listOfficesDTO) {
                    TreeNodeDTO treeNodeDTO = TreeNodeDTO.builder()
                            .id("office_" + employeeMinistryId + "_" + officesDTO.getId() + "_0_root")
                            .icon("fa fa-bank")
                            .text("<span style='color: blue'>" + officesDTO.getNameBangla() + "</span>")
                            .build();

                    treeNodeDTO.setChildren(true);
                    listTreeNodeDTO.add(treeNodeDTO);
                }
            } else {
                TreeNodeDTO treeNodeDTO = TreeNodeDTO.builder()
                        .id("")
                        .icon("fa fa-bank")
                        .children(false)
                        .text("<i>তথ্য নেই</i>")
                        .build();

                listTreeNodeDTO.add(treeNodeDTO);
            }
        } else {
            String[] splittedNodeId = nodeId.split("_");
            String type = splittedNodeId[0];
            officeMinistryId = splittedNodeId[1];
            parentOfficeId = splittedNodeId[2];
            List<Office> listOfficesDTO = officeDAO.findByMinistryAndParentOfficeId(this.officeMinistryService.getOfficeMinistry(Long.parseLong(officeMinistryId)), Long.parseLong(parentOfficeId));
            if (listOfficesDTO.size() > 0) {
                for (Office officesDTO : listOfficesDTO) {
                    if (officesDTO.getParentOffice().getId() == Long.parseLong(parentOfficeId)) {
                        TreeNodeDTO node = TreeNodeDTO.builder()
                                .id("office_" + officeMinistryId + "_" + officesDTO.getId())
                                .dataId("office_" + officeMinistryId + "_" + officesDTO.getId())
                                .text("<span style='color: blue'>" + officesDTO.getNameBangla() + "</span>")
                                .icon("fa fa-caret-square-o-right")
                                .build();

                        if (officesDTO.getOfficeLayer().getLayerLevel() >= 5) {
                            node.setChildren(false);
                        } else {
                            List<Office> listChildOfficesDTO = officeDAO.findByMinistryAndParentOfficeId(this.officeMinistryService.getOfficeMinistry(Long.parseLong(officeMinistryId)), officesDTO.getId());
                            if (listChildOfficesDTO.size() > 0) {
                                node.setChildren(true);
                            } else {
                                node.setChildren(false);
                            }
                        }
                        listTreeNodeDTO.add(node);
                    }
                }
            } else {
                TreeNodeDTO treeNodeDTO = TreeNodeDTO.builder()
                        .id("")
                        .icon("fa fa-ban")
                        .children(false)
                        .text("<i>তথ্য নেই</i>")
                        .build();

                listTreeNodeDTO.add(treeNodeDTO);
            }
        }
        return listTreeNodeDTO;
    }

    public List<TreeNodeOfficerDTO> getSOOrganogram(String id, Authentication authentication) {
        String nodeId = id;
        String officeMinistryId;
        String officeId;
        List<TreeNodeOfficerDTO> listTreeNodeDTO = new ArrayList<>();
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return null;
        }
        Long employeeMinistryId = userInformation.getOfficeInformation().getOfficeMinistryId();
        Long employeeOfficeId = userInformation.getOfficeInformation().getOfficeId();
        if (nodeId.equals("#")) {
            TreeNodeOfficerDTO node = new TreeNodeOfficerDTO();
            node.setId("units_" + employeeMinistryId + "_" + employeeOfficeId + "_0_root");
            node.setDataId("units_" + employeeMinistryId + "_" + employeeOfficeId + "_0_root");
            node.setText("শাখাসমূহ");
            node.setIcon("fa fa-caret-square-o-right");
            node.setChildren(true);
            listTreeNodeDTO.add(node);

        } else {
            String[] splittedNodeId = nodeId.split("_");
            String type = splittedNodeId[0];
            officeMinistryId = splittedNodeId[1];
            officeId = splittedNodeId[2];
            long parentOfficeUnitId = Long.parseLong(splittedNodeId[3]);
            String nodeExt = "";
            if (splittedNodeId.length == 5) {
                nodeExt = splittedNodeId[4];
            }

            if (nodeExt.equals("root")) {
                List<OfficeUnitDTO> listOfficeUnitsDTO = officeUnitDAO.getDTOsByOfficeMinistryIDAndOfficeIdAndParentUnitId(Long.parseLong(officeMinistryId), Long.parseLong(officeId), parentOfficeUnitId);
                if (listOfficeUnitsDTO.size() > 0) {
                    for (OfficeUnitDTO officeUnitsDTO : listOfficeUnitsDTO) {
                        TreeNodeOfficerDTO node = new TreeNodeOfficerDTO();
                        node.setId("units_" + officeMinistryId + "_" + officeId + "_" + officeUnitsDTO.getId() + "_post");
                        node.setDataId("units_" + officeMinistryId + "_" + officeId + "_" + officeUnitsDTO.getId() + "_post");
                        node.setText("<span style='color: blue'>" + officeUnitsDTO.getUnitNameBng() + "</span>");
                        node.setIcon("fa fa-caret-square-o-right");
                        node.setChildren(true);
                        listTreeNodeDTO.add(node);
                    }
                } else {
                    TreeNodeOfficerDTO treeNodeDTO = new TreeNodeOfficerDTO();
                    treeNodeDTO.setId("");
                    treeNodeDTO.setIcon("fa fa-ban");
                    treeNodeDTO.setChildren(false);
                    //treeNodeDTO.setText("<a href='javascript:;' data-id='" + officeLayersDTO.getId() + "' data-ministry-id='" + ministryId + "' data-parent-layer-id='" + parentLayerId + "' data-name-eng='" + officeLayersDTO.getLayerNameEng() + "' data-name-bng='" + officeLayersDTO.getLayerNameBng() + "' onclick='LayerSetup.gotoEdit(this)'>" + officeLayersDTO.getLayerNameBng() + "</a>&nbsp;<a href='javascript:;' onclick='removeNode(this)' style='color:red'><i class='fa fa-minus-circle'></i></a>");
                    treeNodeDTO.setText("<i>তথ্য নেই</i>");
                    listTreeNodeDTO.add(treeNodeDTO);
                }
            } else if (nodeExt.equals("post")) {
                List<OfficeUnitOrganogramDTO> listOfficeUnitOrganogramsDTO = officeUnitOrganogramDAO.getPostsListByUnit(parentOfficeUnitId);
                if (listOfficeUnitOrganogramsDTO.size() > 0) {
                    for (OfficeUnitOrganogramDTO officeUnitOrganogramsDTO : listOfficeUnitOrganogramsDTO) {

                        EmployeeOffice employeeOffice = this.officeService
                                .findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(
                                        officeUnitOrganogramsDTO.getOfficeId(),
                                        officeUnitOrganogramsDTO.getId(), true);
                        TreeNodeOfficerDTO node = new TreeNodeOfficerDTO();
                        if (employeeOffice != null) {
                            String name = employeeOffice.getEmployeeRecord().getNameBangla();
                            String designation = employeeOffice.getDesignation();
                            String officeUnitName = employeeOffice.getOfficeUnit() == null ? "" : employeeOffice.getOfficeUnit().getUnitNameBangla();
                            node.setName(name);
                            node.setId("post_" + officeMinistryId + "_" + officeId + "_" + officeUnitOrganogramsDTO.getId());
                            node.setDataId("post_" + officeMinistryId + "_" + officeId + "_" + officeUnitOrganogramsDTO.getId());
                            node.setText("<span style='color: #052d54'>" + officeUnitOrganogramsDTO.getDesignationBng() + "</span>,<span style='color: #1b1109'> " + employeeOffice.getEmployeeRecord().getNameBangla() + "</span>");
                            node.setDesignation(designation);
                            node.setOfficeUnitName(officeUnitName);
                            node.setIcon("fa fa-user");
                            node.setChildren(false);
                            listTreeNodeDTO.add(node);
                        }
                    }
                } else {
                    TreeNodeOfficerDTO treeNodeDTO = new TreeNodeOfficerDTO();
                    treeNodeDTO.setId("");
                    treeNodeDTO.setIcon("fa fa-ban");
                    treeNodeDTO.setChildren(false);
                    //treeNodeDTO.setText("<a href='javascript:;' data-id='" + officeLayersDTO.getId() + "' data-ministry-id='" + ministryId + "' data-parent-layer-id='" + parentLayerId + "' data-name-eng='" + officeLayersDTO.getLayerNameEng() + "' data-name-bng='" + officeLayersDTO.getLayerNameBng() + "' onclick='LayerSetup.gotoEdit(this)'>" + officeLayersDTO.getLayerNameBng() + "</a>&nbsp;<a href='javascript:;' onclick='removeNode(this)' style='color:red'><i class='fa fa-minus-circle'></i></a>");
                    treeNodeDTO.setText("<i>তথ্য নেই</i>");
                    listTreeNodeDTO.add(treeNodeDTO);
                }
            } else {
                TreeNodeOfficerDTO node = new TreeNodeOfficerDTO();
                node.setId("units_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId);
                node.setDataId("units_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId);
                node.setText("শাখাসমূহ");
                node.setIcon("fa fa-caret-square-o-right");
                //node.setChildren(true);

                List<OfficeUnitDTO> listChildOfficeUnitsDTO = officeUnitDAO.getDTOsByOfficeMinistryIDAndOfficeIdAndParentUnitId(Long.parseLong(officeMinistryId), Long.parseLong(officeId), parentOfficeUnitId);
                if (listChildOfficeUnitsDTO.size() > 0) {
                    node.setChildren(true);
                } else {
                    node.setChildren(false);
                }
                listTreeNodeDTO.add(node);
                TreeNodeOfficerDTO node2 = new TreeNodeOfficerDTO();
                node2.setId("posts_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId + "_post");
                node2.setDataId("posts_" + officeMinistryId + "_" + officeId + "_" + parentOfficeUnitId + "_post");
                node2.setText("পদসমূহ");
                node2.setIcon("fa fa-users");
                //node2.setChildren(true);
                List<OfficeUnitOrganogramDTO> listChildOfficeUnitOrganogramsDTO = officeUnitOrganogramDAO.getPostsListByUnit(parentOfficeUnitId);
                if (listChildOfficeUnitOrganogramsDTO.size() > 0) {
                    node2.setChildren(true);
                } else {
                    node2.setChildren(false);
                }
                listTreeNodeDTO.add(node2);
            }
        }
        return listTreeNodeDTO;
    }

    public List<OfficeUnitOrganogram> findOfficeUnitOrganogramByOfficeOriginUnitOrgIdAndOfficeId(Long officeOriginUnitOrgId, Long officeId) {
        return this.officeUnitOrganogramDAO.findOfficeUnitOrganogramByOfficeOriginUnitOrgIdAndOfficeId(officeOriginUnitOrgId, officeId);
    }

    public List<OfficeOriginUnitOrganogramDTO> getOfficeOriginUnitOrganogramsByOfficeOriginId(Long officeOriginId) {
        List<OfficeOriginUnit> officeOriginUnits = this.officeUnitDAO.getOfficeOriginUnitsByOfficeOriginId(officeOriginId);
        List<OfficeOriginUnitOrganogramDTO> officeOriginUnitOrganogramDTOList = this.officeUnitOrganogramDAO
                .getOfficeOriginUnitOrganogramsByOfficeOriginUnits(officeOriginUnits)
                .stream()
                .map(officeOriginUnitOrganogram -> {
                    OfficeOriginUnit officeOriginUnit = this.officeOriginUnitDAO.findOne(officeOriginUnitOrganogram.getOfficeOriginUnitId());
                    return OfficeOriginUnitOrganogramDTO.builder()
                            .officeOriginId(officeOriginId)
                            .officeOriginUnitId(officeOriginUnitOrganogram.getOfficeOriginUnitId())
                            .officeOriginUnitOrganogramId(officeOriginUnitOrganogram.getId())
                            .designationBangla(officeOriginUnitOrganogram.getNameBangla())
                            .designationEnglish(officeOriginUnitOrganogram.getNameEnglish())
                            .unitNameBangla(officeOriginUnit.getNameBangla())
                            .unitNameEnglish(officeOriginUnit.getNameEnglish())
                            .build();
                })
                .collect(Collectors.toList());
        return officeOriginUnitOrganogramDTOList;
    }
}