package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.ResumoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCategoriaCaixa;

import java.time.LocalDate;
import java.util.List;

public interface ConsultarFluxoCaixaUseCase {

    /** Totais do período (datas opcionais e inclusivas, pela data de referência de cada movimentação). */
    ResumoCaixa consultarResumo(LocalDate inicio, LocalDate fim);

    /** Saldo acumulado desde o início até a data informada (ou até hoje, se nula). */
    ResumoCaixa consultarSaldoAcumulado(LocalDate ate);

    List<ResumoCategoriaCaixa> consultarResumoPorCategoria(LocalDate inicio, LocalDate fim);
}
