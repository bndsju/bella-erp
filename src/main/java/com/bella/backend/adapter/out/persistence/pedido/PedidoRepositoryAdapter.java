package com.bella.backend.adapter.out.persistence.pedido;

import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.PedidoItem;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase.FiltroListagem;
import com.bella.backend.domain.pedido.port.out.PedidoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository pedidoJpaRepository;

    public PedidoRepositoryAdapter(PedidoJpaRepository pedidoJpaRepository) {
        this.pedidoJpaRepository = pedidoJpaRepository;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity salvo = pedidoJpaRepository.save(paraEntidade(pedido));
        return paraDominio(salvo);
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return pedidoJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Pedido> listar(FiltroListagem filtro) {
        return pedidoJpaRepository.buscarPorFiltro(filtro.clienteId(), filtro.status()).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return pedidoJpaRepository.existsById(id);
    }

    private PedidoJpaEntity paraEntidade(Pedido pedido) {
        List<PedidoItemJpaEntity> itens = pedido.getItens().stream()
                .map(item -> new PedidoItemJpaEntity(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getValorUnitario(),
                        item.getSubtotal()))
                .toList();

        return new PedidoJpaEntity(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getOrcamentoOrigemId(),
                pedido.getTransportadoraId(),
                itens,
                pedido.getPercentualDesconto(),
                pedido.getValorFrete(),
                pedido.getSubtotal(),
                pedido.getValorDesconto(),
                pedido.getValorTotal(),
                pedido.getCondicaoPagamento(),
                pedido.getStatus(),
                pedido.getCriadoEm(),
                pedido.getAtualizadoEm());
    }

    private Pedido paraDominio(PedidoJpaEntity entidade) {
        List<PedidoItem> itens = entidade.getItens().stream()
                .map(item -> PedidoItem.existente(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getValorUnitario()))
                .toList();

        return Pedido.existente(
                entidade.getId(),
                entidade.getClienteId(),
                entidade.getOrcamentoOrigemId(),
                entidade.getTransportadoraId(),
                itens,
                entidade.getPercentualDesconto(),
                entidade.getValorFrete(),
                entidade.getCondicaoPagamento(),
                entidade.getStatus(),
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }
}
