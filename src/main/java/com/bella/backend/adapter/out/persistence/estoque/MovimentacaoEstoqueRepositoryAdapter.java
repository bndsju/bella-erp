package com.bella.backend.adapter.out.persistence.estoque;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase.FiltroListagem;
import com.bella.backend.domain.estoque.port.out.MovimentacaoEstoqueRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MovimentacaoEstoqueRepositoryAdapter implements MovimentacaoEstoqueRepositoryPort {

    private final MovimentacaoEstoqueJpaRepository movimentacaoEstoqueJpaRepository;

    public MovimentacaoEstoqueRepositoryAdapter(MovimentacaoEstoqueJpaRepository movimentacaoEstoqueJpaRepository) {
        this.movimentacaoEstoqueJpaRepository = movimentacaoEstoqueJpaRepository;
    }

    @Override
    public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
        MovimentacaoEstoqueJpaEntity salvo = movimentacaoEstoqueJpaRepository.save(paraEntidade(movimentacao));
        return paraDominio(salvo);
    }

    @Override
    public List<MovimentacaoEstoque> listar(FiltroListagem filtro) {
        return movimentacaoEstoqueJpaRepository
                .buscarPorFiltro(filtro.produtoId(), filtro.tipo(), filtro.dataInicio(), filtro.dataFim()).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public List<MovimentacaoEstoque> listarPorOrigemOperacaoId(UUID origemOperacaoId) {
        return movimentacaoEstoqueJpaRepository.findByOrigemOperacaoIdOrderByDataHoraAsc(origemOperacaoId).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorOrigemOperacaoIdETipo(UUID origemOperacaoId, TipoMovimentacaoEstoque tipo) {
        return movimentacaoEstoqueJpaRepository.existsByOrigemOperacaoIdAndTipo(origemOperacaoId, tipo);
    }

    private MovimentacaoEstoqueJpaEntity paraEntidade(MovimentacaoEstoque movimentacao) {
        return new MovimentacaoEstoqueJpaEntity(
                movimentacao.getId(), movimentacao.getProdutoId(), movimentacao.getTipo(),
                movimentacao.getQuantidade(), movimentacao.getMotivo(), movimentacao.getObservacao(),
                movimentacao.getOrigem(), movimentacao.getOrigemOperacaoId(), movimentacao.getUsuarioResponsavel(),
                movimentacao.getDataHora());
    }

    private MovimentacaoEstoque paraDominio(MovimentacaoEstoqueJpaEntity entidade) {
        return MovimentacaoEstoque.existente(
                entidade.getId(), entidade.getProdutoId(), entidade.getTipo(), entidade.getQuantidade(),
                entidade.getMotivo(), entidade.getObservacao(), entidade.getOrigem(),
                entidade.getOrigemOperacaoId(), entidade.getUsuarioResponsavel(), entidade.getDataHora());
    }
}
