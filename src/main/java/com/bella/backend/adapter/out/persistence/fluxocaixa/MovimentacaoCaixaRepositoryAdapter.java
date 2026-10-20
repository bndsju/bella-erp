package com.bella.backend.adapter.out.persistence.fluxocaixa;

import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.port.out.MovimentacaoCaixaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MovimentacaoCaixaRepositoryAdapter implements MovimentacaoCaixaRepositoryPort {

    private final MovimentacaoCaixaJpaRepository movimentacaoCaixaJpaRepository;

    public MovimentacaoCaixaRepositoryAdapter(MovimentacaoCaixaJpaRepository movimentacaoCaixaJpaRepository) {
        this.movimentacaoCaixaJpaRepository = movimentacaoCaixaJpaRepository;
    }

    @Override
    public MovimentacaoCaixa salvar(MovimentacaoCaixa movimentacao) {
        MovimentacaoCaixaJpaEntity salvo = movimentacaoCaixaJpaRepository.save(paraEntidade(movimentacao));
        return paraDominio(salvo);
    }

    @Override
    public Optional<MovimentacaoCaixa> buscarPorId(UUID id) {
        return movimentacaoCaixaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<MovimentacaoCaixa> buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa origem,
                                                                   UUID origemReferenciaId) {
        return movimentacaoCaixaJpaRepository.findByOrigemAndOrigemReferenciaId(origem, origemReferenciaId)
                .map(this::paraDominio);
    }

    @Override
    public List<MovimentacaoCaixa> listar(TipoMovimentacaoCaixa tipo, UUID categoriaId,
                                           OrigemMovimentacaoCaixa origem, SituacaoMovimentacaoCaixa situacao,
                                           boolean incluirCanceladas) {
        return movimentacaoCaixaJpaRepository.buscarPorFiltro(tipo, categoriaId, origem, situacao, incluirCanceladas)
                .stream()
                .map(this::paraDominio)
                .toList();
    }

    private MovimentacaoCaixaJpaEntity paraEntidade(MovimentacaoCaixa movimentacao) {
        return new MovimentacaoCaixaJpaEntity(
                movimentacao.getId(), movimentacao.getTipo(), movimentacao.getDescricao(),
                movimentacao.getCategoriaId(), movimentacao.getValor(), movimentacao.getSituacao(),
                movimentacao.getDataPrevista(), movimentacao.getDataEfetiva(), movimentacao.getOrigem(),
                movimentacao.getOrigemReferenciaId(), movimentacao.getObservacoes(), movimentacao.isCancelada(),
                movimentacao.getMotivoCancelamento(), movimentacao.getCanceladaEm(), movimentacao.getCriadoEm(),
                movimentacao.getAtualizadoEm());
    }

    private MovimentacaoCaixa paraDominio(MovimentacaoCaixaJpaEntity entidade) {
        return MovimentacaoCaixa.existente(
                entidade.getId(), entidade.getTipo(), entidade.getDescricao(), entidade.getCategoriaId(),
                entidade.getValor(), entidade.getSituacao(), entidade.getDataPrevista(), entidade.getDataEfetiva(),
                entidade.getOrigem(), entidade.getOrigemReferenciaId(), entidade.getObservacoes(),
                entidade.isCancelada(), entidade.getMotivoCancelamento(), entidade.getCanceladaEm(),
                entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
