package com.bitfomo.common.fetch;

import java.time.LocalDateTime;

public interface DefaultFetchProvider {
    public String fetchInformation();
    public String fetchWhenInformation(LocalDateTime startTime, LocalDateTime endTime);
}
