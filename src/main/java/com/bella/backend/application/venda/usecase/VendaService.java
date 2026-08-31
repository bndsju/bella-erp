package com.bella.backend.application.venda.usecase;

import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.BuscarPedidoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.model.VendaItem;
import com.bella.backend.domain.venda.port.in.BuscarVendaPorIdUseCase;
import com.bella.backend.domain.venda.port.in.CancelarVendaUseCase;
import com.bella.backend.domain.venda.port.in.ConcluirVendaUseCase;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase;
import com.bella.backend.domain.venda.port.out.VendaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VendaService implements
        ConcluirVendaUseCase,
        BuscarVendaPorIdUseCase,
        ListarVendasUseCase,
        CancelarVendaUseCase {

    private final VendaRepositoryPort vendaRepositoryPort;
    private final BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;

    public VendaService(VendaRepositoryPort vendaRepositoryPort, BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase) {
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.buscarPedidoPorIdUseCase = buscarPedidoPorIdUseCase;
    }

    @Override
    public Venda concluir(UUID pedidoId, FormaPagamento formaPagamento) {
        Pedido pedido = buscarPedidoPorIdUseCase.buscarPorId(pedidoId);

        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraDeNegocioException("Só é possível concluir a venda de um pedido entregue");
        }
        if (vendaRepositoryPort.existePorPedidoId(pedidoId)) {
            throw new RegraDeNegocioException("Este pedido já foi convertido em venda");
        }

        List<VendaItem> itens = pedido.getItens().stream()
                .map(item -> VendaItem.novo(item.getProdutoId(), item.getQuantidade(), item.getValorUnitario()))
                .toList();

        Venda venda = Venda.concluir(
                pedido.getId(),
                pedido.getClienteId(),
                itens,
                pedido.getPercentualDesconto(),
                pedido.getValorFrete(),
                formaPagamento,
                pedido.getCondicaoPagamento());

        return vendaRepositoryPort.salvar(venda);
    }

    @Override
    @Transactional(readOnly = true)
    public Venda buscarPorId(UUID id) {
        return vendaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Venda", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venda> listar(FiltroListagem filtro) {
        return vendaRepositoryPort.listar(filtro);
    }

    @Override
    public Venda cancelar(UUID id) {
        Venda venda = buscarPorId(id);
        venda.cancelar();
        return vendaRepositoryPort.salvar(venda);
    }
}
