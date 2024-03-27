package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 14-Mar-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingDTO {
    private String id;
    private String meetingDate;
    private String subject;
    private Boolean active;
}
