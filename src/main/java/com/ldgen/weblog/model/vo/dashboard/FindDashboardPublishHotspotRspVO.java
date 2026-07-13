package com.ldgen.weblog.model.vo.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindDashboardPublishHotspotRspVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> dateList;

    private List<Long> articlePublishCountList;
}
