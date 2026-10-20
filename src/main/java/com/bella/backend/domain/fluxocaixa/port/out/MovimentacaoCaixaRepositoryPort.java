package com.bella.backend.domain.fluxocaixa.port.out;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimentacaoCaixaRepositoryPort {

    MovimentacaoCaixa salvar(MovimentacaoCaixa movimentacao);

    Optional<MovimentacaoCaixa> buscarPorId(UUID id);

    Optional<MovimentacaoCaixa> buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId);

    /** Filtros simples resolvidos no banco; o recorte por período fica na aplicação. */
    List<MovimentacaoCaixa> listar(TipoMovimentacaoCaixa tipo, UUID categoriaId, OrigemMovimentacaoCaixa origem,
                                    SituacaoMovimentacaoCaixa situacao, boolean incluirCanceladas);
}
