package com.grs.api.myGov;


import com.grs.api.model.request.ComplainantDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MyGovUserResponse implements Serializable {

    HashMap<String,MyGovUser> data;


}
