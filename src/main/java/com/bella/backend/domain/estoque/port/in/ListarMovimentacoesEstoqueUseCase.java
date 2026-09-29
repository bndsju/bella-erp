package com.bella.backend.domain.estoque.port.in;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListarMovimentacoesEstoqueUseCase {

    List<MovimentacaoEstoque> listar(FiltroListagem filtro);

    record FiltroListagem(UUID produtoId, TipoMovimentacaoEstoque tipo, Instant dataInicio, Instant dataFim) {
    }
}
