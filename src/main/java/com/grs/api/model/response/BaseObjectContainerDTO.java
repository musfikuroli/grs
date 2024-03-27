package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Tanvir on 8/30/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseObjectContainerDTO {
    public List<BaseObjectDTO> objects;
}
