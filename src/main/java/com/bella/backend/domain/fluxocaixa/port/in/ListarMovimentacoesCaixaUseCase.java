package com.bella.backend.domain.fluxocaixa.port.in;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListarMovimentacoesCaixaUseCase {

    List<MovimentacaoCaixa> listar(FiltroListagem filtro);

    /**
     * O período filtra pela data de referência: a data efetiva das realizadas e a data prevista
     * das previstas (limites inclusivos). Canceladas só aparecem com incluirCanceladas.
     * Ordenado da data de referência mais recente para a mais antiga.
     */
    record FiltroListagem(TipoMovimentacaoCaixa tipo, UUID categoriaId, OrigemMovimentacaoCaixa origem,
                           SituacaoMovimentacaoCaixa situacao, LocalDate dataInicio, LocalDate dataFim,
                           boolean incluirCanceladas) {
    }
}
