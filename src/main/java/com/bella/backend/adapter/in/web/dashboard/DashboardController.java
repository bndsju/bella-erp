package com.bella.backend.adapter.in.web.dashboard;

import com.bella.backend.domain.dashboard.model.Dashboard;
import com.bella.backend.domain.dashboard.port.in.ConsultarDashboardUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ConsultarDashboardUseCase consultarDashboardUseCase;

    public DashboardController(ConsultarDashboardUseCase consultarDashboardUseCase) {
        this.consultarDashboardUseCase = consultarDashboardUseCase;
    }

    @GetMapping
    public Dashboard consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "5") int limite) {
        return consultarDashboardUseCase.consultar(inicio, fim, limite);
    }
}
