package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Created by Tanvir on 8/24/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    @NotNull(message="Username not present")
    @Size(min = 4, max = 75,message = "Username is invalid")
    private String username;

    @Pattern(regexp = "^(?=.*\\d).{6,15}$")
    private String password;

    @Pattern(regexp = "(?:[a-z0-9!#$%&'*+=?^_`{|}~-]+" +
            "(?:\\.[a-z0-9!#$%&'*+=?^_`{|}~-]+)*|\"" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
            "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]" +
            "?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:" +
            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",message = "Invalid email")
    private String email;

    @Pattern(regexp = "^[0][1][1-9][0-9]{8,8}$")
    private String mobileNumber;

    @Pattern(regexp = "^[0-9]{10,10}$|^[0-9]{13,13}$|^[0-9]{17,17}$||^[0-9]{0,0}$")
    private String nationalID;

    @Size(min = 4, max = 75)
    private String name;

    private String gender;

    @Size(min = 4, max = 75)
    private String mothersName;

    private String maleGurdiansType;

    @Size(min = 4, max = 75)
    private String maleGurdiansName;

    @Size(min = 4, max = 75)
    private String presentAddress;

    @Size(min = 4, max = 75)
    private String permanentAddress;

}
