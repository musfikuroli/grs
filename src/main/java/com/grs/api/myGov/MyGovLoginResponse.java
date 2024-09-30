package com.grs.api.myGov;

import com.google.gson.Gson;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.api.sso.SSOPropertyReader;
import com.grs.core.domain.IdentificationType;
import com.grs.core.domain.grs.Complainant;
import com.grs.utils.BanglaConverter;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.WeakHashMap;

@Slf4j
public class MyGovLoginResponse {

    public static Gson gson;

    private SSOPropertyReader ssoPropertyReader;
    private String sessionNonce;

    private WeakHashMap<String, String> mygovToGRSgenderMAp = new WeakHashMap<>();
    private WeakHashMap<String, String> grsToMygovgenderMAp = new WeakHashMap<>();

    public MyGovLoginResponse() throws Exception{
        gson = new Gson();
        ssoPropertyReader = SSOPropertyReader.getInstance();
        this.sessionNonce = "";

        grsToMygovgenderMAp.put("FEMALE", "female");
        grsToMygovgenderMAp.put("MALE", "male");
        grsToMygovgenderMAp.put("OTHER", "other");

        mygovToGRSgenderMAp.put("female", "FEMALE");
        mygovToGRSgenderMAp.put("male", "MALE");
        mygovToGRSgenderMAp.put("other", "OTHER");
    }

    private String getSharedSecret() throws Exception{
        return ssoPropertyReader.getSecret();
    }

    private void validateResponse(String response) throws Exception{
        if(response == null || response.equals("")){
            throw new Exception("Response null or empty");
        }
    }

    private void validateSecret(String secret) throws Exception{
        if(secret == null || secret.equals("")){
            throw new Exception("Secret null or empty");
        }
    }

    public void setSessionNonce(String sessionNonce){
        this.sessionNonce = sessionNonce;
    }

    public ComplainantDTO claimToGenericDTO(MyGovUserDTO claims){
        ComplainantDTO complainantDTO = new ComplainantDTO();
        complainantDTO.setIdentificationType(IdentificationType.NID.toString());
        if (StringUtil.isValidString(claims.getMobile()))complainantDTO.setPhoneNumber(claims.getMobile());
        if (StringUtil.isValidString(claims.getPassword()))complainantDTO.setPinNumber(claims.getPassword());
        if (StringUtil.isValidString(claims.getEmail()))complainantDTO.setEmail(claims.getEmail());
        if (StringUtil.isValidString(claims.getNid()))complainantDTO.setIdentificationValue(claims.getNid());
        if (StringUtil.isValidString(claims.getName()))complainantDTO.setName(claims.getName());
        if (StringUtil.isValidString(claims.getGender()))complainantDTO.setGender(mygovToGRSgenderMAp.get(claims.getGender()));
        if (claims.getDate_of_birth() != null && !claims.getDate_of_birth().isEmpty()) {
            String[] dobParts = claims.getDate_of_birth().split("-");
            String newDob = dobParts[2] + "/" + dobParts[1] + "/" + dobParts[0];
            complainantDTO.setBirthDate(newDob);
        }
        if (StringUtil.isValidString(claims.getOccupation()))complainantDTO.setOccupation(claims.getOccupation());
        if (StringUtil.isValidString(claims.getPre_address()))complainantDTO.setPresentAddressHouse(claims.getPre_address());
        if (StringUtil.isValidString(claims.getPer_address()))complainantDTO.setPermanentAddressHouse(claims.getPer_address());

        return complainantDTO;
    }

    public MyGovUserDTO genericDTOToClaims(ComplainantDTO complainantDTO){
        MyGovUserDTO myGovUserDTO = new MyGovUserDTO();
        if (StringUtil.isValidString(complainantDTO.getPhoneNumber()))myGovUserDTO.setMobile(complainantDTO.getPhoneNumber());
        if (StringUtil.isValidString(complainantDTO.getPinNumber()))myGovUserDTO.setPassword(complainantDTO.getPinNumber());
        if (StringUtil.isValidString(complainantDTO.getEmail()))myGovUserDTO.setEmail(complainantDTO.getEmail());
        if (StringUtil.isValidString(complainantDTO.getIdentificationValue()))myGovUserDTO.setNid(BanglaConverter.convertAllToEnglish(complainantDTO.getIdentificationValue()));
        if (StringUtil.isValidString(complainantDTO.getName()))myGovUserDTO.setName(complainantDTO.getName());
        if (StringUtil.isValidString(complainantDTO.getGender()))myGovUserDTO.setGender(grsToMygovgenderMAp.get(complainantDTO.getGender()));
        if (complainantDTO.getBirthDate() != null && !complainantDTO.getBirthDate().isEmpty()) {
            String[] dobParts = complainantDTO.getBirthDate().split("/");
            String newDob = dobParts[2] + "-" + dobParts[1] + "-" + dobParts[0];
            myGovUserDTO.setDate_of_birth(newDob);
        }
        if (StringUtil.isValidString(complainantDTO.getOccupation()))myGovUserDTO.setOccupation(complainantDTO.getOccupation());
        if (StringUtil.isValidString(complainantDTO.getPresentAddressHouse()))myGovUserDTO.setPre_address(complainantDTO.getPresentAddressHouse());
        if (StringUtil.isValidString(complainantDTO.getPermanentAddressHouse()))myGovUserDTO.setPer_address(complainantDTO.getPermanentAddressHouse());

        return myGovUserDTO;
    }

    public MyGovUserDTO genericToClaims(Complainant complainant){
        String json = gson.toJson(complainant);

        ComplainantDTO dto = gson.fromJson(json, ComplainantDTO.class);
        return genericDTOToClaims(dto);
    }

    public final ComplainantDTO parseResponse(String response)throws Exception  {
        MyGovUserDTO myGovUserDTO = gson.fromJson(response, MyGovUserDTO.class);

        ComplainantDTO complainantDTO = claimToGenericDTO(myGovUserDTO);

        log.info("MyGov response parsed successfully");
        return complainantDTO;
    }

}
