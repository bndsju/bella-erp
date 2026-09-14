package com.bella.backend.application.pedido.usecase;

import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.orcamento.port.in.BuscarOrcamentoPorIdUseCase;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.PedidoItem;
import com.bella.backend.domain.pedido.port.in.BuscarPedidoPorIdUseCase;
import com.bella.backend.domain.pedido.port.in.CadastrarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.CancelarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.ComandoItemPedido;
import com.bella.backend.domain.pedido.port.in.ComandoPedido;
import com.bella.backend.domain.pedido.port.in.ConfirmarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.CriarPedidoAPartirDeOrcamentoUseCase;
import com.bella.backend.domain.pedido.port.in.EditarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.EntregarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.IniciarSeparacaoPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase;
import com.bella.backend.domain.pedido.port.in.MarcarProntoParaEntregaPedidoUseCase;
import com.bella.backend.domain.pedido.port.out.PedidoRepositoryPort;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.transportadora.port.in.BuscarTransportadoraPorIdUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PedidoService implements
        CadastrarPedidoUseCase,
        CriarPedidoAPartirDeOrcamentoUseCase,
        EditarPedidoUseCase,
        BuscarPedidoPorIdUseCase,
        ListarPedidosUseCase,
        ConfirmarPedidoUseCase,
        IniciarSeparacaoPedidoUseCase,
        MarcarProntoParaEntregaPedidoUseCase,
        EntregarPedidoUseCase,
        CancelarPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepositoryPort;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase;
    private final BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase;

    public PedidoService(PedidoRepositoryPort pedidoRepositoryPort,
                          BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                          BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                          BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase,
                          BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase) {
        this.pedidoRepositoryPort = pedidoRepositoryPort;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.buscarTransportadoraPorIdUseCase = buscarTransportadoraPorIdUseCase;
        this.buscarOrcamentoPorIdUseCase = buscarOrcamentoPorIdUseCase;
    }

    @Override
    public Pedido cadastrar(ComandoPedido comando) {
        validarReferencias(comando.clienteId(), comando.transportadoraId(), comando.itens());

        Pedido pedido = Pedido.novo(
                comando.clienteId(),
                null,
                comando.transportadoraId(),
                paraItens(comando.itens()),
                comando.percentualDesconto(),
                comando.valorFrete(),
                comando.condicaoPagamento());

        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido criarAPartirDeOrcamento(UUID orcamentoId, UUID transportadoraId) {
        Orcamento orcamento = buscarOrcamentoPorIdUseCase.buscarPorId(orcamentoId);
        if (orcamento.getStatus() != StatusOrcamento.APROVADO) {
            throw new RegraDeNegocioException("Só é possível criar um pedido a partir de um orçamento aprovado");
        }
        if (transportadoraId != null) {
            buscarTransportadoraPorIdUseCase.buscarPorId(transportadoraId);
        }

        List<PedidoItem> itens = orcamento.getItens().stream()
                .map(item -> PedidoItem.novo(item.getProdutoId(), item.getQuantidade(), item.getValorUnitario()))
                .toList();

        Pedido pedido = Pedido.novo(
                orcamento.getClienteId(),
                orcamento.getId(),
                transportadoraId,
                itens,
                orcamento.getPercentualDesconto(),
                orcamento.getValorFrete(),
                orcamento.getCondicaoPagamento());

        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido editar(UUID id, ComandoPedido comando) {
        Pedido pedido = buscarPorId(id);
        validarReferencias(comando.clienteId(), comando.transportadoraId(), comando.itens());

        pedido.editar(
                comando.clienteId(),
                comando.transportadoraId(),
                paraItens(comando.itens()),
                comando.percentualDesconto(),
                comando.valorFrete(),
                comando.condicaoPagamento());

        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido buscarPorId(UUID id) {
        return pedidoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pedido", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listar(FiltroListagem filtro) {
        return pedidoRepositoryPort.listar(filtro);
    }

    @Override
    public Pedido confirmar(UUID id) {
        Pedido pedido = buscarPorId(id);
        pedido.confirmar();
        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido iniciarSeparacao(UUID id) {
        Pedido pedido = buscarPorId(id);
        pedido.iniciarSeparacao();
        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido marcarProntoParaEntrega(UUID id) {
        Pedido pedido = buscarPorId(id);
        pedido.marcarProntoParaEntrega();
        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido entregar(UUID id) {
        Pedido pedido = buscarPorId(id);
        pedido.entregar();
        return pedidoRepositoryPort.salvar(pedido);
    }

    @Override
    public Pedido cancelar(UUID id) {
        Pedido pedido = buscarPorId(id);
        pedido.cancelar();
        return pedidoRepositoryPort.salvar(pedido);
    }

    private void validarReferencias(UUID clienteId, UUID transportadoraId, List<ComandoItemPedido> itens) {
        buscarClientePorIdUseCase.buscarPorId(clienteId);
        if (transportadoraId != null) {
            buscarTransportadoraPorIdUseCase.buscarPorId(transportadoraId);
        }
        for (ComandoItemPedido item : itens) {
            buscarProdutoPorIdUseCase.buscarPorId(item.produtoId());
        }
    }

    private List<PedidoItem> paraItens(List<ComandoItemPedido> itensComando) {
        return itensComando.stream()
                .map(item -> PedidoItem.novo(item.produtoId(), item.quantidade(), item.valorUnitario()))
                .toList();
    }
}
