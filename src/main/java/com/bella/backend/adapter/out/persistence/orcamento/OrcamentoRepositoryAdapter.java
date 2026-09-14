package com.bella.backend.adapter.out.persistence.orcamento;

import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.OrcamentoItem;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase.FiltroListagem;
import com.bella.backend.domain.orcamento.port.out.OrcamentoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrcamentoRepositoryAdapter implements OrcamentoRepositoryPort {

    private final OrcamentoJpaRepository orcamentoJpaRepository;

    public OrcamentoRepositoryAdapter(OrcamentoJpaRepository orcamentoJpaRepository) {
        this.orcamentoJpaRepository = orcamentoJpaRepository;
    }

    @Override
    public Orcamento salvar(Orcamento orcamento) {
        OrcamentoJpaEntity salvo = orcamentoJpaRepository.save(paraEntidade(orcamento));
        return paraDominio(salvo);
    }

    @Override
    public Optional<Orcamento> buscarPorId(UUID id) {
        return orcamentoJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Orcamento> listar(FiltroListagem filtro) {
        return orcamentoJpaRepository.buscarPorFiltro(filtro.clienteId(), filtro.status()).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return orcamentoJpaRepository.existsById(id);
    }

    private OrcamentoJpaEntity paraEntidade(Orcamento orcamento) {
        List<OrcamentoItemJpaEntity> itens = orcamento.getItens().stream()
                .map(item -> new OrcamentoItemJpaEntity(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getValorUnitario(),
                        item.getSubtotal()))
                .toList();

        return new OrcamentoJpaEntity(
                orcamento.getId(),
                orcamento.getClienteId(),
                itens,
                orcamento.getPercentualDesconto(),
                orcamento.getValorFrete(),
                orcamento.getSubtotal(),
                orcamento.getValorDesconto(),
                orcamento.getValorTotal(),
                orcamento.getPrazoValidade(),
                orcamento.getCondicaoPagamento(),
                orcamento.getObservacoes(),
                orcamento.getStatus(),
                orcamento.getCriadoEm(),
                orcamento.getAtualizadoEm());
    }

    private Orcamento paraDominio(OrcamentoJpaEntity entidade) {
        List<OrcamentoItem> itens = entidade.getItens().stream()
                .map(item -> OrcamentoItem.existente(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getValorUnitario()))
                .toList();

        return Orcamento.existente(
                entidade.getId(),
                entidade.getClienteId(),
                itens,
                entidade.getPercentualDesconto(),
                entidade.getValorFrete(),
                entidade.getPrazoValidade(),
                entidade.getCondicaoPagamento(),
                entidade.getObservacoes(),
                entidade.getStatus(),
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }
}
