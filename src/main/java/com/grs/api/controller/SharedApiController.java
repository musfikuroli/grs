package com.grs.api.controller;

import com.google.gson.Gson;
import com.grs.api.annotation.SwaggerInclude;
import com.grs.api.model.request.MyGovRequestDTO;
import com.grs.api.model.request.FileDTO;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import com.grs.api.model.response.*;
import com.grs.api.model.response.dashboard.DashboardDataDTO;
import com.grs.api.model.response.dashboard.ItemIdNameCountDTO;
import com.grs.api.model.response.file.DerivedFileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.api.model.response.grievance.GrievanceDetailsDTO;
import com.grs.api.model.response.grievance.OISFDashboardDTO;
import com.grs.api.model.response.grievance.OISFGrievanceListDTO;
import com.grs.api.model.response.grievance.OISFIntermediateDashboardDTO;
import com.grs.api.sso.GeneralInboxDataDTO;
import com.grs.core.dao.ApiClientDAO;
import com.grs.core.dao.ComplainantDAO;
import com.grs.core.dao.SuperAdminDAO;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.ApiClient;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.SuperAdmin;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.model.EmptyJsonResponse;
import com.grs.core.service.*;
import com.grs.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@Api(description = "Shared API list of GRS")
public class SharedApiController {

    @Autowired
    private OfficeService officeService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private GrievanceService grievanceForwardingService;
    @Autowired
    private GrievanceForwardingService actualGrievanceForwardingService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private ApiClientDAO apiClientDAO;
    @Autowired
    private ComplainantDAO complainantDAO;
    @Autowired
    private SuperAdminDAO grsUserDAO;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private Gson gson;

    @SwaggerInclude
    @ApiOperation(value = "Add new grievance")
    @RequestMapping(value = "/api/shared/public/grievance", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WeakHashMap<String, String> addGrievance(Authentication authentication, @RequestBody GrievanceRequestDTO grievanceRequestDTO) throws Exception {
        return this.grievanceService.addGrievance(authentication, grievanceRequestDTO);
    }

    @SwaggerInclude
    @ApiOperation(value = "View details of a grievance")
    @RequestMapping(value = "/api/shared/public/grievance/{id}", method = RequestMethod.GET)
    public GrievanceDetailsDTO getGrievanceDetails(Authentication authentication, @PathVariable("id") Long id) {
        return grievanceService.getGrievanceDetails(id);
    }


    @SwaggerInclude
    @ApiOperation(value = "View current status of a grievance")
    @RequestMapping(value = "/api/shared/public/grievance/status", method = RequestMethod.GET)
    public Object getStatusOfGrievance(@RequestParam("trackingNumber") String trackingNumber, @RequestParam("phoneNumber") String phoneNumber) {
        return this.grievanceService.getStatusOfGrievance(trackingNumber, phoneNumber);
    }

    @SwaggerInclude
    @ApiOperation(value = "Returns only services included in citizens charter an office")
    @RequestMapping(value = "/api/shared/public/offices/{office_id}/citizens-charter", method = RequestMethod.GET)
    public Page<CitizenCharterDTO> getCitizenChartersByOffice(
            @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable,
            @PathVariable("office_id") Long officeID) {
        return officeService.getAllCitizenChartersByOffice(pageable, officeID);
    }

    @SwaggerInclude
    @ApiOperation(value = "View Grievance Redress Officer (GRO) information")
    @RequestMapping(value = "/api/shared/public/offices/{officeId}/gro", method = RequestMethod.GET)
    public GroContactInfoResponseDTO getGRO(@PathVariable("officeId") Long officeId) {
        return this.officeService.getGROcontactInfoByOfficeId(officeId);
    }

    @SwaggerInclude
    @ApiOperation(value = "View Appeal Officer (AO) information")
    @RequestMapping(value = "/api/shared/public/offices/{officeId}/ao", method = RequestMethod.GET)
    public GroContactInfoResponseDTO getAO(@PathVariable("officeId") Long officeId) {
        return this.officeService.getAoContactInfoByOfficeId(officeId);
    }

    @SwaggerInclude
    @ApiOperation(value = "View citizens charter with vision, mission, expectations, services, GRO and AO information")
    @RequestMapping(value = "/api/shared/public/offices/{office_id}/citizens-charter-details", method = RequestMethod.GET)
    public Object getOfficesCitizenChartersForOffice(@PathVariable("office_id") Long officeId) {
        try {
            Office office = officeService.findOne(officeId);
            if (office == null) {
                throw new Exception("Office doesn't exist");
            }
            Long officeOriginId = office.getOfficeOriginId();
            OfficeLayer officeLayer = office.getOfficeLayer();
            if (officeLayer == null) {
                throw new Exception("Office layer doesn't exist");
            }
            Integer layerLevel = officeLayer.getLayerLevel();
            return CitizenChartersByOfficeDTO.builder()
                    .officeNameBangla(office.getNameBangla())
                    .officeNameEnglish(office.getNameEnglish())
                    .websiteUrl(office.getWebsiteUrl())
                    .visionMission(officeService.getOfficesVisionMission(layerLevel.longValue(), officeOriginId))
                    .citizenServices(officeService.getServicesByServiceTypeFromOfficesCitizenCharter(officeId, ServiceType.NAGORIK))
                    .officialServices(officeService.getServicesByServiceTypeFromOfficesCitizenCharter(officeId, ServiceType.DAPTORIK))
                    .internalServices(officeService.getServicesByServiceTypeFromOfficesCitizenCharter(officeId, ServiceType.STAFF))
                    .officeGRO(officeService.getGRODetailsByOfficeId(officeId))
                    .officeAO(officeService.getAODetailsByOfficeId(officeId))
                    .build();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return GenericResponse.builder().success(false).message("Error with office related information").build();
        }
    }

    @RequestMapping(value = "/token/create", method = RequestMethod.POST)
    public WeakHashMap<String, String> createAccessToken(ServletRequest req, ServletResponse res) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String auth = request.getHeader(Constant.API_CLIENT_TOKEN_HEADER);
        if(!StringUtil.isValidString(auth)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid api request header");
            return null;
        }
        String[] authValues = auth.trim().replaceAll("\\s+", " ").split(" ");
        if(Constant.API_CLIENT_SECRET_PREFIX.equals(authValues[0]) && StringUtil.isValidString(authValues[1])) {
            String appSecret = authValues[1];
            ApiClient apiClient = apiClientDAO.getByAppSecret(appSecret);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Secret Key");
                return null;
            }
            String accessToken = UUID.randomUUID().toString().replace("-", "");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, Constant.API_CLIENT_TOKEN_DURATION);
            apiClient.setAccessToken(accessToken);
            apiClient.setExpiryTime(calendar.getTime());
            apiClient.setUpdatedAt(new Date());
            apiClient = apiClientDAO.save(apiClient);
            String newToken = apiClient.getAccessToken();
            return new WeakHashMap() {{
                put("token", newToken);
            }};
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Secret");
            return null;
        }
    }

    @RequestMapping(value = "/api/v2/token/create", method = RequestMethod.POST)
    public DoptorTokenCreateResponseDTO createAccessTokenV2(ServletRequest req, ServletResponse res, @RequestBody MyGovRequestDTO myGovRequestDTO) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String accept = request.getHeader(Constant.DOPTOR_API_CLIENT_HEADER_ACCEPT);
        String contentType = request.getHeader(Constant.DOPTOR_API_CLIENT_HEADER_CONT_TYPE);
        if(!StringUtil.isValidString(accept) || !StringUtil.isValidString(contentType)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid api request header");
            return null;
        }
        if(StringUtil.isValidString(myGovRequestDTO.getUsername()) && StringUtil.isValidString(myGovRequestDTO.getSecret())) {
            String appName = myGovRequestDTO.getUsername();
            Complainant complainant = complainantDAO.findByUsername(appName);
            SuperAdmin grsUser = grsUserDAO.findByUsername(appName);
            ApiClient apiClient = apiClientDAO.getByAppName(appName);
            if(apiClient == null && complainant == null && grsUser == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret Key");
                return null;
            }
            boolean complainantMatched = complainant != null && bCryptPasswordEncoder.matches(myGovRequestDTO.getSecret(), complainant.getPassword());
            boolean grsUserMatched = grsUser != null && bCryptPasswordEncoder.matches(myGovRequestDTO.getSecret(), grsUser.getPassword());
            boolean apiClientMatched = apiClient != null && bCryptPasswordEncoder.matches(myGovRequestDTO.getSecret(), apiClient.getAppSecret());
            if(!complainantMatched && !grsUserMatched && !apiClientMatched) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret Key");
                return null;
            }
            if (apiClient == null) apiClient = new ApiClient();
            String accessToken = UUID.randomUUID().toString().replace("-", "");
            String refreshToken = UUID.randomUUID().toString().replace("-", "");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, Constant.API_CLIENT_TOKEN_DURATION);
            apiClient.setAccessToken(accessToken);
            apiClient.setRefreshToken(refreshToken);
            apiClient.setExpiryTime(calendar.getTime());
            apiClient.setUpdatedAt(new Date());
            apiClient.setAppName(myGovRequestDTO.getUsername());
            apiClient.setAppSecret(myGovRequestDTO.getSecret());
            apiClient = apiClientDAO.save(apiClient);
            DoptorTokenCreateResponseDTO responseDTO = new DoptorTokenCreateResponseDTO();
            responseDTO.setAccess_token(accessToken);
            responseDTO.setRefresh_token(refreshToken);
            return responseDTO;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret");
            return null;
        }
    }

    @RequestMapping(value = "/api/v2/token/refresh", method = RequestMethod.POST)
    public DoptorTokenCreateResponseDTO refreshAccessTokenV2(ServletRequest req, ServletResponse res, @RequestBody MyGovRequestDTO myGovRequestDTO) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String refreshToken = request.getHeader(Constant.HEADER_STRING);
        if(!StringUtil.isValidString(refreshToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid api request header");
            return null;
        }
        refreshToken = refreshToken.replace(Constant.TOKEN_PREFIX, "").trim();
        if(true) {
            ApiClient apiClient = apiClientDAO.getByRefreshToken(refreshToken);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
                return null;
            }
            String accessToken = UUID.randomUUID().toString().replace("-", "");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, Constant.API_CLIENT_TOKEN_DURATION);
            apiClient.setAccessToken(accessToken);
            apiClient.setRefreshToken(refreshToken);
            apiClient.setExpiryTime(calendar.getTime());
            apiClient.setUpdatedAt(new Date());
            apiClient = apiClientDAO.save(apiClient);
            DoptorTokenCreateResponseDTO responseDTO = new DoptorTokenCreateResponseDTO();
            responseDTO.setAccess_token(accessToken);
            responseDTO.setRefresh_token(refreshToken);
            return responseDTO;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Secret");
            return null;
        }
    }

    @RequestMapping(value = "/api/submit/complaint", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WeakHashMap<String, Object> addGrievanceWithoutLogin(ServletRequest req, ServletResponse res,  @RequestParam String complaint,
                                                            @RequestParam(required = false) MultipartFile[] files) throws Exception {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        MyGovRequestDTO myGovRequestDTO = new MyGovRequestDTO();

        try {
            myGovRequestDTO = gson.fromJson(complaint, MyGovRequestDTO.class);
        } catch (Exception e) {
            response.sendError(703, "Incorrect Json format for: complaint");
            return null;
        }

        if(StringUtil.isValidString(myGovRequestDTO.getUsername()) && StringUtil.isValidString(myGovRequestDTO.getSecret())) {
            String appName = myGovRequestDTO.getUsername();
            String appSecret = myGovRequestDTO.getSecret();
            ApiClient apiClient = apiClientDAO.getByAppSecretAndAppName(appSecret, appName);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret Key");
                return null;
            }

            String jsonString = gson.toJson(myGovRequestDTO);
            GrievanceWithoutLoginRequestDTO grievanceRequestDTO = gson.fromJson(jsonString, GrievanceWithoutLoginRequestDTO.class);

            if(!StringUtil.isValidString(myGovRequestDTO.getOfficeId())) {
                response.sendError(700, "Missing required field: officeId");
                return null;
            }

            if(!StringUtil.isValidString(myGovRequestDTO.getBody())) {
                response.sendError(700, "Missing required field: body");
                return null;
            }

            if (!Utility.isNumber(grievanceRequestDTO.getOfficeId())) {
                response.sendError(600, "Illegal format of number for: officeId");
                return null;
            }

            if (!Utility.isNumber(grievanceRequestDTO.getComplainantPhoneNumber()) && !(grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous())) {
                response.sendError(600, "Illegal format of number for: complainantPhoneNumber");
                return null;
            }

            if (!Utility.isNumber(grievanceRequestDTO.getComplainantPhoneNumber()) && !(grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous())) {
                response.sendError(600, "Complainant Phone number is required or should be anonymous");
                return null;
            }

            if(StringUtil.isValidString(myGovRequestDTO.getComplainantPhoneNumber())) {
                Complainant complainant = this.complainantService.findComplainantByPhoneNumber(myGovRequestDTO.getComplainantPhoneNumber());
                if (complainant != null) {
                    List<Long> blacklistInOfficeId  = complainantService.findBlacklistedOffices(complainant.getId());
                    if (blacklistInOfficeId.contains(Long.parseLong(myGovRequestDTO.getOfficeId()))) {
                        response.sendError(800, "Sorry, this complainant cannot complain to this office!");
                        return null;
                    }
                }
            }

            if (files != null && files.length > 0) {
                DerivedFileContainerDTO fileContainerDTO = this.storageService.storeDerivedFile(null, files);

                List<FileDTO> grievanceFiles = new ArrayList<>();
                for (FileDerivedDTO fileDerivedDTO: fileContainerDTO.getFiles()) {
                    grievanceFiles.add(FileDTO.builder()
                            .name(fileDerivedDTO.getName())
                            .url(fileDerivedDTO.getUrl())
                            .build());
                }

                grievanceRequestDTO.setFiles(grievanceFiles);
            }

            if (myGovRequestDTO.getFileUriList() != null && !myGovRequestDTO.getFileUriList().isEmpty()) {

                for (String rawUrL: myGovRequestDTO.getFileUriList()) {

                    URL url = new URL(rawUrL);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream is = null;
                    try {
                        is = url.openStream ();
                        byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
                        int n;

                        while ( (n = is.read(byteChunk)) > 0 ) {
                            baos.write(byteChunk, 0, n);
                        }

                        String fileName = rawUrL;
                        CustomMultipartFile customMultipartFile = new CustomMultipartFile(baos.toByteArray(), fileName);

                        DerivedFileContainerDTO fileContainerDTO = this.storageService.storeDerivedFile(null, new CustomMultipartFile[] { customMultipartFile });

                        List<FileDTO> grievanceFiles = new ArrayList<>();
                        for (FileDerivedDTO fileDerivedDTO: fileContainerDTO.getFiles()) {
                            grievanceFiles.add(FileDTO.builder()
                                    .name(fileDerivedDTO.getName())
                                    .url(fileDerivedDTO.getUrl())
                                    .build());
                        }

                        if (grievanceRequestDTO.getFiles() == null) grievanceRequestDTO.setFiles(new ArrayList<>());
                        grievanceRequestDTO.getFiles().addAll(grievanceFiles);

                    }
                    catch (IOException e) {
                        System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
                        e.printStackTrace ();
                        // Perform any other exception handling that's appropriate.
                    }
                    finally {
                        if (is != null) { is.close(); }
                        baos.close();
                    }

                }
            }

            grievanceRequestDTO.setServiceId("0");
            grievanceRequestDTO.setServiceOthers("Others");
            grievanceRequestDTO.setServiceType(ServiceType.NAGORIK);
            grievanceRequestDTO.setSubmittedThroughApi(1);
            WeakHashMap<String, Object> returnObject = this.grievanceService.addGrievanceWithoutLogin(null, grievanceRequestDTO);
            if (returnObject.get("error") != null && returnObject.get("error").equals("No GRO")) {
                returnObject.put("message", "Grievance Redress Officer not found");
            }
            return returnObject;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret");
            return null;
        }
    }

    @RequestMapping(value = "/api/grievanceByTrackingNumber", method = RequestMethod.POST)
    public Object getGrievancesByTrackingNumber(ServletRequest req, ServletResponse res,  @RequestParam(required = true) String body) throws IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        MyGovRequestDTO myGovRequestDTO = new MyGovRequestDTO();

        try {
            myGovRequestDTO = gson.fromJson(body, MyGovRequestDTO.class);
        } catch (Exception e) {
            response.sendError(703, "Incorrect Json format for: body");
            return null;
        }

        if(StringUtil.isValidString(myGovRequestDTO.getUsername()) && StringUtil.isValidString(myGovRequestDTO.getSecret())) {
            String appName = myGovRequestDTO.getUsername();
            String appSecret = myGovRequestDTO.getSecret();
            ApiClient apiClient = apiClientDAO.getByAppSecretAndAppName(appSecret, appName);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret Key");
                return null;
            }

            if(!StringUtil.isValidString(myGovRequestDTO.getTrackingNumber())) {
                response.sendError(700, "Missing required field: trackingNumber");
                return null;
            }

            return this.grievanceService.getGrievanceByTrackingNumber(myGovRequestDTO.getTrackingNumber());

        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret");
            return null;
        }
    }



    @RequestMapping(value = "/api/grievancesByPhoneNumber", method = RequestMethod.POST)
    public Object getGrievancesByPhoneNumber(ServletRequest req, ServletResponse res,  @RequestParam(required = true) String body) throws IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        MyGovRequestDTO myGovRequestDTO = new MyGovRequestDTO();

        try {
            myGovRequestDTO = gson.fromJson(body, MyGovRequestDTO.class);
        } catch (Exception e) {
            response.sendError(703, "Incorrect Json format for: body");
            return null;
        }

        if(StringUtil.isValidString(myGovRequestDTO.getUsername()) && StringUtil.isValidString(myGovRequestDTO.getSecret())) {
            String appName = myGovRequestDTO.getUsername();
            String appSecret = myGovRequestDTO.getSecret();
            ApiClient apiClient = apiClientDAO.getByAppSecretAndAppName(appSecret, appName);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret Key");
                return null;
            }

            if(!StringUtil.isValidString(myGovRequestDTO.getComplainantPhoneNumber())) {
                response.sendError(700, "Missing required field: complainantPhoneNumber");
                return null;
            }


            Complainant complainant = this.complainantService.findComplainantByPhoneNumber(myGovRequestDTO.getComplainantPhoneNumber());
            if (complainant == null) {
                return new EmptyJsonResponse();
            }
            return this.grievanceService.getGrievancesByComplainantIdForApi(complainant.getId());
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret");
            return null;
        }
    }

    @RequestMapping(value = "/api/grievanceHistoryByTrackingNumber", method = RequestMethod.POST)
    public List<GrievanceForwardingEmployeeRecordsDTO> grievanceHistoryByTrackingNumber(ServletRequest req, ServletResponse res,  @RequestParam(required = true) String body) throws IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        MyGovRequestDTO myGovRequestDTO = new MyGovRequestDTO();

        try {
            myGovRequestDTO = gson.fromJson(body, MyGovRequestDTO.class);
        } catch (Exception e) {
            response.sendError(703, "Incorrect Json format for: body");
            return null;
        }

        if(StringUtil.isValidString(myGovRequestDTO.getUsername()) && StringUtil.isValidString(myGovRequestDTO.getSecret())) {
            String appName = myGovRequestDTO.getUsername();
            String appSecret = myGovRequestDTO.getSecret();
            ApiClient apiClient = apiClientDAO.getByAppSecretAndAppName(appSecret, appName);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret Key");
                return null;
            }

            if(!StringUtil.isValidString(myGovRequestDTO.getTrackingNumber())) {
                response.sendError(700, "Missing required field: trackingNumber");
                return null;
            }

            return this.actualGrievanceForwardingService.getAllComplainantComplaintMovementHistoryByTrackingNumber(myGovRequestDTO.getTrackingNumber());
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Username/Secret");
            return null;
        }
    }

    @RequestMapping(value = "/token/get", method = RequestMethod.GET)
    public WeakHashMap<String, String> getAccessToken(ServletRequest req, ServletResponse res) throws IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String auth = request.getHeader(Constant.API_CLIENT_TOKEN_HEADER);
        if(!StringUtil.isValidString(auth)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid api request header");
            return null;
        }
        String[] authValues = auth.trim().replaceAll("\\s+", " ").split(" ");
        if(Constant.API_CLIENT_SECRET_PREFIX.equals(authValues[0]) && StringUtil.isValidString(authValues[1])) {
            String appSecret = authValues[1];
            ApiClient apiClient = apiClientDAO.getByAppSecret(appSecret);
            if(apiClient == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Secret Key");
                return null;
            }
            String accessToken = apiClient.getAccessToken();
            Date expiryTime = apiClient.getExpiryTime();
            Long timeDiff = expiryTime.getTime() - (new Date()).getTime();
            Long duration = timeDiff > 0L ? TimeUnit.SECONDS.convert(timeDiff, TimeUnit.MILLISECONDS) : 0L;
            return new WeakHashMap() {{
                put("token", accessToken);
                put("duration", duration.toString());
            }};
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Secret");
            return null;
        }
    }

    @SwaggerInclude
    @ApiOperation(value = "View frequency of grievances")
    @RequestMapping(value = "/api/shared/public/services/comp/freq", method = RequestMethod.GET)
    public List<ItemIdNameCountDTO> getCitizenCharterServicesByComplaintFrequency() {
        return dashboardService.getCitizenCharterServicesByComplaintFrequency();
    }

    @SwaggerInclude
    @ApiOperation(value = "View GRO current month info")
    @RequestMapping(value = "/api/shared/public/details/office/{office_id}/{office_unit_organogram_id}", method = RequestMethod.GET)
    public DashboardDataDTO getDashboardData(@PathVariable("office_id") Long officeId, @PathVariable("office_unit_organogram_id") Long officeUnitOrganogramId) {
        return DashboardDataDTO.builder()
                .groDashboardData(dashboardService.getGrievanceDataForGRODashboard(officeId, true))
                .build();
    }

    @SwaggerInclude
    @ApiOperation(value = "user inbox and dashboard info")
    @RequestMapping(value = "/api/shared/public/inbox", method = RequestMethod.GET)
    public OISFGrievanceListDTO getUserInfo(@RequestParam("office_id") Long officeId, @RequestParam("designation_id") Long officeUnitOrganogramId, @RequestParam("user_id") Long userId) {
        OISFIntermediateDashboardDTO oisfIntermediateDashboardDTO = this.grievanceForwardingService.getInboxDataDTO(officeId, officeUnitOrganogramId, userId);
        GeneralInboxDataDTO generalInboxDataDTO = oisfIntermediateDashboardDTO.getGeneralInboxDataDTO();
        List<OISFDashboardDTO> dashboardDTOS = new ArrayList<>();
        OISFDashboardDTO dashboardDTO;
        for (int i = 0; i <= 4; i++) {
            dashboardDTO = new OISFDashboardDTO();
            if (i == 0) {
                dashboardDTO.setCount(BanglaConverter.convertToBanglaDigit(oisfIntermediateDashboardDTO.getGrievanceDTOS().size()));
                dashboardDTO.setTitle("মোট অভিযোগ");
                dashboardDTO.setIcon("icon-arrow-down");
                dashboardDTO.setRedirectURL("http://www.grs.gov.bd/login?a=1&redirectUrl=viewGrievances.do");
                dashboardDTO.setDisplayOrder(i + 1);
            } else if (i == 1) {
                dashboardDTO.setCount(BanglaConverter.convertToBanglaDigit(generalInboxDataDTO.getInbox().getValue()));
                dashboardDTO.setTitle(generalInboxDataDTO.getInbox().getName());
                dashboardDTO.setIcon("icon-arrow-down");
                dashboardDTO.setRedirectURL("http://www.grs.gov.bd/login?a=1&redirectUrl=viewGrievances.do");
                dashboardDTO.setDisplayOrder(i + 1);
            } else if (i == 2) {
                dashboardDTO.setCount(BanglaConverter.convertToBanglaDigit(generalInboxDataDTO.getOutbox().getValue()));
                dashboardDTO.setTitle(generalInboxDataDTO.getOutbox().getName());
                dashboardDTO.setIcon("icon-arrow-right");
                dashboardDTO.setRedirectURL("http://www.grs.gov.bd/login?a=1&redirectUrl=viewGrievances.do");
                dashboardDTO.setDisplayOrder(i + 1);
            } else if (i == 3) {
                dashboardDTO.setCount(BanglaConverter.convertToBanglaDigit(generalInboxDataDTO.getResolved().getValue()));
                dashboardDTO.setTitle(generalInboxDataDTO.getResolved().getName());
                dashboardDTO.setIcon("icon-check");
                dashboardDTO.setRedirectURL("http://www.grs.gov.bd/login?a=1&redirectUrl=viewGrievances.do");
                dashboardDTO.setDisplayOrder(i + 1);
            } else {
                dashboardDTO.setCount(BanglaConverter.convertToBanglaDigit(generalInboxDataDTO.getForwarded().getValue()));
                dashboardDTO.setTitle(generalInboxDataDTO.getForwarded().getName());
                dashboardDTO.setIcon("icon-trash");
                dashboardDTO.setRedirectURL("http://www.grs.gov.bd/login?a=1&redirectUrl=viewGrievances.do");
                dashboardDTO.setDisplayOrder(i + 1);
            }
            dashboardDTOS.add(dashboardDTO);
        }
        OISFGrievanceListDTO dto = OISFGrievanceListDTO.builder()
                .summary(oisfIntermediateDashboardDTO.getGrievanceDTOS())
                .dashboard(dashboardDTOS)
                .designation_id(officeUnitOrganogramId)
                .build();
        return dto;
//        return this.grievanceForwardingService.getUserInboxList(65L, 89104L, 3078L);
    }

}
