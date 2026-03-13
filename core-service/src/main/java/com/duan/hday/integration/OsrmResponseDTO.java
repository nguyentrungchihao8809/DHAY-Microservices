package com.duan.hday.integration;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmResponseDTO {
    private String code;
    private List<OsrmRouteDTO> routes;
}