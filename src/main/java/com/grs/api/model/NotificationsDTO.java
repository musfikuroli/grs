package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsDTO {
    private String countBangla;
    private Long count;
    private List<NotificationDTO> notifications;
}
