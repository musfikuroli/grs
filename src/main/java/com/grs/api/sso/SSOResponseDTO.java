package com.grs.api.sso;

import com.grs.core.domain.RedirectMap;

public class SSOResponseDTO {
    private String username;
    private long   employee_record_id;
    private long   office_id;
    private String designation;
    private long   office_unit_id;
    private long   incharge_label;
    private long   office_unit_organogram_id;
    private String office_name_eng;
    private String office_name_bng;
    private long   office_ministry_id;
    private String office_ministry_name_eng;
    private String office_ministry_name_bng;
    private String unit_name_eng;
    private String unit_name_bng;
    private String landing_page_url;
    private long   expiration_time;
    private String nonce;
    private RedirectMap redirectMap;
    private String sub;

    public void setUsername(String username){this.username = username;}
    public void setEmployeeRecordId(long employeeRecordId){this.employee_record_id = employeeRecordId;}
    public void setOfficeId(long officeId){this.office_id = officeId;}
    public void setDesignation(String designation){this.designation = designation;}
    public void setOfficeUnitId(long officeUnitId){this.office_unit_id = officeUnitId;}
    public void setInchargeLabel(long inchargeLabel){this.incharge_label = inchargeLabel;}
    public void setOfficeUnitOrgId(long officeUnitOrgId){this.office_unit_organogram_id = officeUnitOrgId;}
    public void setOfficeNameEng(String officeNameEng){this.office_name_eng = officeNameEng;}
    public void setOfficeNameBng(String officeNameBng){this.office_name_bng = officeNameBng;}
    public void setOfficeMinistryId(long officeMinistryId){this.office_ministry_id = officeMinistryId;}
    public void setOfficeMinistryNameEng(String nameEng){this.office_ministry_name_eng = nameEng;}
    public void setOfficeMinistryNameBng(String nameBng){this.office_ministry_name_bng = nameBng;}
    public void setUnitNameEng(String nameEng){this.unit_name_eng = nameEng;}
    public void setUnitNameBng(String nameBng){this.unit_name_bng = nameBng;}
    public void setLandingPageUrl(String landingPageUrl){this.landing_page_url = landingPageUrl;}
    public void setExpirationTime(long time){this.expiration_time = time;}
    public void setNonce(String nonce){this.nonce = nonce;}
    public void setSub(String sub){this.sub = sub;}

    public void setRedirectMap(String redirectMap) {
        this.redirectMap = RedirectMap.valueOf(redirectMap);
    }

    public String getUsername(){return this.username;}
    public long getEmployeeRecordId(){return this.employee_record_id;}
    public long getOfficeId(){return this.office_id;}
    public String getDesignation(){return this.designation;}
    public long getOfficeUnitId(){return this.office_unit_id;}
    public long getInchargeLabel(){return this.incharge_label;}
    public long getOfficeUnitOrgId(){return this.office_unit_organogram_id;}
    public String getOfficeNameEng(){return this.office_name_eng;}
    public String getOfficeNameBng(){return this.office_name_bng;}
    public long getOfficeMinistryId(){return this.office_ministry_id;}
    public String getOfficeMinistryNameEng(){return this.office_ministry_name_eng;}
    public String getOfficeMinistryNameBng(){return this.office_ministry_name_bng;}
    public String getUnitNameEng(){return this.unit_name_eng;}
    public String getUnitNameBng(){return this.unit_name_bng;}
    public String getLandingPageUrl(){return this.landing_page_url;}
    public long getExpirationTime(){return this.expiration_time;}
    public String getNonce(){return this.nonce;}
    public String getSub(){return this.sub;}

    public RedirectMap getRedirectMap() {
        return this.redirectMap;
    }
}