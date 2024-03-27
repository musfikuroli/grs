package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * Created by Acer on 05-Oct-17.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenericResponse {
    public boolean success;
    public String message;

}
