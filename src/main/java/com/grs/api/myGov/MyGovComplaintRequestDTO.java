package com.grs.api.myGov;

import com.grs.api.model.request.GrievanceWithoutLoginRequestDTO;
import lombok.Data;

@Data
public class MyGovComplaintRequestDTO {
        public String user = "";
        public String password = "";
        public String name = "";
        public String email = "";
        public String subject = "";
        public String body = "";
        public String officeId = "";
        public String complainantPhoneNumber = "";

       public MyGovComplaintRequestDTO convertToMyGovComplaintRequestDTO(GrievanceWithoutLoginRequestDTO grievanceWithoutLoginRequestDTO) {
               MyGovComplaintRequestDTO requestDTO = new MyGovComplaintRequestDTO();
               requestDTO.setUser(grievanceWithoutLoginRequestDTO.getUser());
               requestDTO.setPassword(grievanceWithoutLoginRequestDTO.getSecret());
               requestDTO.setName(grievanceWithoutLoginRequestDTO.getName());
               requestDTO.setEmail(grievanceWithoutLoginRequestDTO.getEmail());
               requestDTO.setSubject(grievanceWithoutLoginRequestDTO.getSubject());
               requestDTO.setBody(grievanceWithoutLoginRequestDTO.getBody());
               requestDTO.setOfficeId(grievanceWithoutLoginRequestDTO.getOfficeId());
               requestDTO.setComplainantPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber());
               return requestDTO;
       }
}
