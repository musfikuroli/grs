package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 9/6/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceContainerDTO {
    public List<ServiceOriginDTO> services;
}
