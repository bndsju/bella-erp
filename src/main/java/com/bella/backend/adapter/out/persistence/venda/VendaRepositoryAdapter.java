package com.bella.backend.adapter.out.persistence.venda;

import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.model.VendaItem;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase.FiltroListagem;
import com.bella.backend.domain.venda.port.out.VendaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VendaRepositoryAdapter implements VendaRepositoryPort {

    private final VendaJpaRepository vendaJpaRepository;

    public VendaRepositoryAdapter(VendaJpaRepository vendaJpaRepository) {
        this.vendaJpaRepository = vendaJpaRepository;
    }

    @Override
    public Venda salvar(Venda venda) {
        VendaJpaEntity salva = vendaJpaRepository.save(paraEntidade(venda));
        return paraDominio(salva);
    }

    @Override
    public Optional<Venda> buscarPorId(UUID id) {
        return vendaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Venda> listar(FiltroListagem filtro) {
        return vendaJpaRepository.buscarPorFiltro(filtro.clienteId(), filtro.status()).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorPedidoId(UUID pedidoId) {
        return vendaJpaRepository.existsByPedidoId(pedidoId);
    }

    private VendaJpaEntity paraEntidade(Venda venda) {
        List<VendaItemJpaEntity> itens = venda.getItens().stream()
                .map(item -> new VendaItemJpaEntity(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getValorUnitario(),
                        item.getSubtotal()))
                .toList();

        return new VendaJpaEntity(
                venda.getId(),
                venda.getPedidoId(),
                venda.getClienteId(),
                itens,
                venda.getPercentualDesconto(),
                venda.getValorFrete(),
                venda.getSubtotal(),
                venda.getValorDesconto(),
                venda.getValorTotal(),
                venda.getFormaPagamento(),
                venda.getCondicaoPagamento(),
                venda.getStatus(),
                venda.getCriadoEm(),
                venda.getAtualizadoEm());
    }

    private Venda paraDominio(VendaJpaEntity entidade) {
        List<VendaItem> itens = entidade.getItens().stream()
                .map(item -> VendaItem.existente(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getValorUnitario()))
                .toList();

        return Venda.existente(
                entidade.getId(),
                entidade.getPedidoId(),
                entidade.getClienteId(),
                itens,
                entidade.getPercentualDesconto(),
                entidade.getValorFrete(),
                entidade.getFormaPagamento(),
                entidade.getCondicaoPagamento(),
                entidade.getStatus(),
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }
}
