package com.duan.hday.controller.driver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/api/v1/system")
public class SystemCheckController {

    @GetMapping("/time-check")
    public Map<String, Object> checkTimezone() {
        return Map.of(
            "jvm_timezone_id", TimeZone.getDefault().getID(),
            "jvm_local_time", LocalDateTime.now().toString(),
            "jvm_zoned_time", ZonedDateTime.now().toString(),
            "system_timestamp", System.currentTimeMillis()
        );
    }
}
