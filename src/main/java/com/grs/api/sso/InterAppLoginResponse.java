//momi@revesoft.com
package com.grs.api.sso;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Service
public class InterAppLoginResponse {
    private final String oisfJwtSSOEndpoint    = "http://doptor.gov.bd/jwtSSO";//"http://localhost:8082/jwtSSO";   //To Do. should read from database or properties file
    private final String loginToQS             = "loginTo";   //To Do. should read from database or properties file
    private final String requestFromQS         = "requestFrom";   //To Do. should read from database or properties file
    private final String jwtTokenQS            = "token";   //To Do. should read from database or properties file
    private final String userNameClaim         = "username"; //To Do. should read from database or properties file
    private final String secret;

    private JwtBuilder builder;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private SignatureAlgorithm signatureAlgorithm;
    private SSOResponseDTO jwtSSOResponseDTO;

    private String loginTo;
    private String requestFrom;
    private String jwtToken;

    public InterAppLoginResponse(HttpServletRequest request, HttpServletResponse response)throws Exception{
        this.request = request;
        this.response = response;
        builder = null;
        signatureAlgorithm = SignatureAlgorithm.HS256;
        jwtSSOResponseDTO = new SSOResponseDTO();

        this.loginTo = request.getParameter(this.loginToQS);
        this.secret = SSOPropertyReader.getInstance().getSecret();
        this.jwtToken = this.request.getParameter(this.jwtTokenQS);

        if(this.secret == null || this.secret.equals("")){
            throw new Exception(" Secret null or empty. Please put shared secret in properties file ");
        }

        if(this.jwtToken == null || this.jwtToken.equals("")){
            throw new Exception(" Token null or empty. Please provide jwt token ");
        }
    }

    public void performJwtSSOForUser(String username){
        try{
            builder = Jwts.builder()
                    .claim(this.userNameClaim, username)
                    .signWith(signatureAlgorithm, this.secret.getBytes("UTF-8"));

            String jwtToken = builder.compact();

            String requestUrl = this.oisfJwtSSOEndpoint + "?" + this.loginToQS + "="  + this.loginTo + "&" + this.requestFromQS + "=" + this.requestFrom + "&" + this.jwtTokenQS + "=" + jwtToken;
            System.out.println(" jwt sso requestUrl : " + requestUrl);

            response.sendRedirect(requestUrl);
            return;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public SSOResponseDTO processResponse() throws Exception{
        Claims claims = Jwts.parser()
                .setSigningKey(this.secret.getBytes("UTF-8"))
                .parseClaimsJws(this.jwtToken)
                .getBody();

        SSOResponseDTO jwtSSOResponseDTO = new SSOResponseDTO();
        jwtSSOResponseDTO.setUsername(claims.get(JwtSSOClaims.USERNAME).toString());
        jwtSSOResponseDTO.setEmployeeRecordId(Integer.parseInt(claims.get(JwtSSOClaims.EMPLOYEE_RECORD_ID).toString()));
        jwtSSOResponseDTO.setOfficeId(Integer.parseInt(claims.get(JwtSSOClaims.OFFICE_ID).toString()));
        jwtSSOResponseDTO.setDesignation(claims.get(JwtSSOClaims.DESIGNATION).toString());
        jwtSSOResponseDTO.setOfficeUnitId(Integer.parseInt(claims.get(JwtSSOClaims.OFFICE_UNIT_ID).toString()));
        jwtSSOResponseDTO.setInchargeLabel(Integer.parseInt(claims.get(JwtSSOClaims.INCHARGE_LABEL).toString()));
        jwtSSOResponseDTO.setOfficeUnitOrgId(Integer.parseInt(claims.get(JwtSSOClaims.OFFICE_UNIT_ORGANOGRAM_ID).toString()));
        jwtSSOResponseDTO.setOfficeNameEng(claims.get(JwtSSOClaims.OFFICE_NAME_ENG).toString());
        jwtSSOResponseDTO.setOfficeNameBng(claims.get(JwtSSOClaims.OFFICE_NAME_BNG).toString());
        jwtSSOResponseDTO.setOfficeMinistryId(Integer.parseInt(claims.get(JwtSSOClaims.OFFICE_MINISTRY_ID).toString()));
        jwtSSOResponseDTO.setOfficeMinistryNameEng(claims.get(JwtSSOClaims.OFFICE_MINISTRY_NAME_ENG).toString());
        jwtSSOResponseDTO.setOfficeMinistryNameBng(claims.get(JwtSSOClaims.OFFICE_MINISTRY_NAME_BNG).toString());
        jwtSSOResponseDTO.setUnitNameEng(claims.get(JwtSSOClaims.UNIT_NAME_ENG).toString());
        jwtSSOResponseDTO.setUnitNameBng(claims.get(JwtSSOClaims.UNIT_NAME_BNG).toString());
        jwtSSOResponseDTO.setLandingPageUrl(claims.get(JwtSSOClaims.LANDING_PAGE_URL).toString());

        return jwtSSOResponseDTO;
    }
}