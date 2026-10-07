package com.bella.backend.domain.estoque.port.out;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase.FiltroListagem;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoEstoqueRepositoryPort {

    MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao);

    List<MovimentacaoEstoque> listar(FiltroListagem filtro);

    List<MovimentacaoEstoque> listarPorOrigemOperacaoId(UUID origemOperacaoId);

    boolean existePorOrigemOperacaoIdETipo(UUID origemOperacaoId, TipoMovimentacaoEstoque tipo);
}
