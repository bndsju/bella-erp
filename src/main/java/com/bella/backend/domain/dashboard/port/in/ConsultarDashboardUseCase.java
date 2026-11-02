package com.bella.backend.domain.dashboard.port.in;

import com.bella.backend.domain.dashboard.model.Dashboard;

import java.time.LocalDate;

public interface ConsultarDashboardUseCase {

    /** Sem período informado, usa o mês corrente. {@code limiteRankings} limita as listas de destaque. */
    Dashboard consultar(LocalDate inicio, LocalDate fim, int limiteRankings);
}
