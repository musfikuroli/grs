package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 26-Nov-17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpinionRequestDTO {
    Long grievanceId;
    String comment;
    List<FileDTO> files;
    List<String> postNode;
    List<String> ccNode;
    Date deadline;
    List<Long> referredFiles;
}
