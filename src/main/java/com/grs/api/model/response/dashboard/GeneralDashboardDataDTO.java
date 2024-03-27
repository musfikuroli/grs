package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralDashboardDataDTO {
    NameValuePairDTO total;
    NameValuePairDTO resolved;
    NameValuePairDTO declined;
    NameValuePairDTO running;
    NameValuePairDTO unresolved;
    DashboardRatingDTO rating;
}
