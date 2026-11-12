package com.medicamentos.controller;

import com.medicamentos.dto.response.DashboardDTO;
import com.medicamentos.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('VISTA_DASHBOARD:LEER')")
    public DashboardDTO getStats() {
        return dashboardService.getStats();
    }
}
