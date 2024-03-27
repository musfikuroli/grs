package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 13-Mar-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CellMeetingDTO {
    private String subject;
    private String submissionDate;
    private List<String> grievanceIds;
}
