package com.bella.backend.application.estoque.usecase;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.OrigemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;
import com.bella.backend.domain.estoque.model.SaldoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ComandoMovimentacaoManual;
import com.bella.backend.domain.estoque.port.in.ConsultarSaldoEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.ConsumirReservaEstoqueParaPedidoUseCase;
import com.bella.backend.domain.estoque.port.in.ItemEntradaCompra;
import com.bella.backend.domain.estoque.port.in.ItemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.LiberarReservaEstoqueParaPedidoUseCase;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.ListarProdutosAbaixoDoEstoqueMinimoUseCase;
import com.bella.backend.domain.estoque.port.in.ListarSaldoEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarAjusteEntradaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarAjusteSaidaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarEntradaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarEntradasDeCompraUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarSaidaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.ReservarEstoqueParaPedidoUseCase;
import com.bella.backend.domain.estoque.port.out.MovimentacaoEstoqueRepositoryPort;
import com.bella.backend.domain.estoque.port.out.SaldoEstoqueRepositoryPort;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class EstoqueService implements
        RegistrarEntradaEstoqueUseCase,
        RegistrarSaidaEstoqueUseCase,
        RegistrarAjusteEntradaEstoqueUseCase,
        RegistrarAjusteSaidaEstoqueUseCase,
        RegistrarEntradasDeCompraUseCase,
        ReservarEstoqueParaPedidoUseCase,
        LiberarReservaEstoqueParaPedidoUseCase,
        ConsumirReservaEstoqueParaPedidoUseCase,
        ConsultarSaldoEstoqueUseCase,
        ListarSaldoEstoqueUseCase,
        ListarProdutosAbaixoDoEstoqueMinimoUseCase,
        ListarMovimentacoesEstoqueUseCase {

    private static final String MOTIVO_RESERVA = "Reserva de estoque referente ao pedido";
    private static final String MOTIVO_LIBERACAO_RESERVA = "Liberação de reserva referente ao cancelamento do pedido";
    private static final String MOTIVO_SAIDA_ENTREGA = "Saída de estoque referente à entrega do pedido";
    private static final String MOTIVO_ENTRADA_COMPRA = "Entrada de estoque referente ao recebimento da compra";

    private final SaldoEstoqueRepositoryPort saldoEstoqueRepositoryPort;
    private final MovimentacaoEstoqueRepositoryPort movimentacaoEstoqueRepositoryPort;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final ListarProdutosUseCase listarProdutosUseCase;

    public EstoqueService(SaldoEstoqueRepositoryPort saldoEstoqueRepositoryPort,
                           MovimentacaoEstoqueRepositoryPort movimentacaoEstoqueRepositoryPort,
                           BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                           ListarProdutosUseCase listarProdutosUseCase) {
        this.saldoEstoqueRepositoryPort = saldoEstoqueRepositoryPort;
        this.movimentacaoEstoqueRepositoryPort = movimentacaoEstoqueRepositoryPort;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.listarProdutosUseCase = listarProdutosUseCase;
    }

    @Override
    public MovimentacaoEstoque registrarEntrada(ComandoMovimentacaoManual comando) {
        SaldoEstoque saldo = validarProdutoAtivoEBuscarSaldo(comando.produtoId());
        saldo.registrarEntrada(comando.quantidade());
        saldoEstoqueRepositoryPort.salvar(saldo);
        return registrarMovimentacaoManual(comando, TipoMovimentacaoEstoque.ENTRADA);
    }

    @Override
    public MovimentacaoEstoque registrarSaida(ComandoMovimentacaoManual comando) {
        SaldoEstoque saldo = validarProdutoAtivoEBuscarSaldo(comando.produtoId());
        saldo.registrarSaida(comando.quantidade());
        saldoEstoqueRepositoryPort.salvar(saldo);
        return registrarMovimentacaoManual(comando, TipoMovimentacaoEstoque.SAIDA);
    }

    @Override
    public MovimentacaoEstoque registrarAjusteEntrada(ComandoMovimentacaoManual comando) {
        SaldoEstoque saldo = validarProdutoAtivoEBuscarSaldo(comando.produtoId());
        saldo.registrarEntrada(comando.quantidade());
        saldoEstoqueRepositoryPort.salvar(saldo);
        return registrarMovimentacaoManual(comando, TipoMovimentacaoEstoque.AJUSTE_ENTRADA);
    }

    @Override
    public MovimentacaoEstoque registrarAjusteSaida(ComandoMovimentacaoManual comando) {
        SaldoEstoque saldo = validarProdutoAtivoEBuscarSaldo(comando.produtoId());
        saldo.registrarSaida(comando.quantidade());
        saldoEstoqueRepositoryPort.salvar(saldo);
        return registrarMovimentacaoManual(comando, TipoMovimentacaoEstoque.AJUSTE_SAIDA);
    }

    @Override
    public void registrarEntradasDeCompra(UUID compraId, List<ItemEntradaCompra> itens) {
        if (movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(compraId, TipoMovimentacaoEstoque.ENTRADA)) {
            return;
        }

        for (ItemEntradaCompra item : itens) {
            SaldoEstoque saldo = buscarOuCriarSaldo(item.produtoId());
            saldo.registrarEntrada(item.quantidade());
            saldoEstoqueRepositoryPort.salvar(saldo);

            movimentacaoEstoqueRepositoryPort.salvar(MovimentacaoEstoque.registrar(
                    item.produtoId(), TipoMovimentacaoEstoque.ENTRADA, item.quantidade(), MOTIVO_ENTRADA_COMPRA,
                    "Custo unitário: " + item.custoUnitario().toPlainString(), OrigemMovimentacaoEstoque.COMPRA,
                    compraId, null));
        }
    }

    @Override
    public void reservarParaPedido(UUID pedidoId, List<ItemMovimentacaoEstoque> itens) {
        if (movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(pedidoId, TipoMovimentacaoEstoque.RESERVA)) {
            return;
        }

        for (ItemMovimentacaoEstoque item : itens) {
            SaldoEstoque saldo = buscarOuCriarSaldo(item.produtoId());
            saldo.reservar(item.quantidade());
            saldoEstoqueRepositoryPort.salvar(saldo);

            movimentacaoEstoqueRepositoryPort.salvar(MovimentacaoEstoque.registrar(
                    item.produtoId(), TipoMovimentacaoEstoque.RESERVA, item.quantidade(), MOTIVO_RESERVA, null,
                    OrigemMovimentacaoEstoque.PEDIDO, pedidoId, null));
        }
    }

    @Override
    public void liberarReservaParaPedido(UUID pedidoId) {
        List<MovimentacaoEstoque> reservas = reservasDoPedido(pedidoId);
        if (reservas.isEmpty() || jaProcessadoAposReserva(pedidoId)) {
            return;
        }

        for (MovimentacaoEstoque reserva : reservas) {
            SaldoEstoque saldo = buscarSaldoObrigatorio(reserva.getProdutoId());
            saldo.liberarReserva(reserva.getQuantidade());
            saldoEstoqueRepositoryPort.salvar(saldo);

            movimentacaoEstoqueRepositoryPort.salvar(MovimentacaoEstoque.registrar(
                    reserva.getProdutoId(), TipoMovimentacaoEstoque.LIBERACAO_RESERVA, reserva.getQuantidade(),
                    MOTIVO_LIBERACAO_RESERVA, null, OrigemMovimentacaoEstoque.PEDIDO, pedidoId, null));
        }
    }

    @Override
    public void consumirReservaParaPedido(UUID pedidoId) {
        List<MovimentacaoEstoque> reservas = reservasDoPedido(pedidoId);
        if (reservas.isEmpty()) {
            throw new RegraDeNegocioException("Pedido não possui reserva de estoque para consumir");
        }
        if (jaProcessadoAposReserva(pedidoId)) {
            return;
        }

        for (MovimentacaoEstoque reserva : reservas) {
            SaldoEstoque saldo = buscarSaldoObrigatorio(reserva.getProdutoId());
            saldo.consumirReserva(reserva.getQuantidade());
            saldoEstoqueRepositoryPort.salvar(saldo);

            movimentacaoEstoqueRepositoryPort.salvar(MovimentacaoEstoque.registrar(
                    reserva.getProdutoId(), TipoMovimentacaoEstoque.SAIDA, reserva.getQuantidade(),
                    MOTIVO_SAIDA_ENTREGA, null, OrigemMovimentacaoEstoque.PEDIDO, pedidoId, null));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PosicaoEstoqueProduto buscarPorProdutoId(UUID produtoId) {
        Produto produto = buscarProdutoPorIdUseCase.buscarPorId(produtoId);
        SaldoEstoque saldo = buscarOuCriarSaldo(produtoId);
        return PosicaoEstoqueProduto.de(saldo, produto.getEstoqueMinimo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosicaoEstoqueProduto> listarTodos() {
        List<Produto> produtos = listarProdutosUseCase.listar(new ListarProdutosUseCase.FiltroListagem(null, null, null));
        Map<UUID, SaldoEstoque> saldosPorProduto = saldoEstoqueRepositoryPort.listarTodos().stream()
                .collect(Collectors.toMap(SaldoEstoque::getProdutoId, Function.identity()));

        return produtos.stream()
                .map(produto -> PosicaoEstoqueProduto.de(
                        saldosPorProduto.getOrDefault(produto.getId(), SaldoEstoque.novo(produto.getId())),
                        produto.getEstoqueMinimo()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosicaoEstoqueProduto> listarAbaixoDoMinimo() {
        return listarTodos().stream().filter(PosicaoEstoqueProduto::abaixoDoMinimo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoque> listar(FiltroListagem filtro) {
        return movimentacaoEstoqueRepositoryPort.listar(filtro);
    }

    private SaldoEstoque validarProdutoAtivoEBuscarSaldo(UUID produtoId) {
        Produto produto = buscarProdutoPorIdUseCase.buscarPorId(produtoId);
        if (produto.getStatus() != StatusProduto.ATIVO) {
            throw new RegraDeNegocioException("Produto inativo não pode receber movimentações de estoque");
        }
        return buscarOuCriarSaldo(produtoId);
    }

    private MovimentacaoEstoque registrarMovimentacaoManual(ComandoMovimentacaoManual comando,
                                                              TipoMovimentacaoEstoque tipo) {
        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrar(
                comando.produtoId(), tipo, comando.quantidade(), comando.motivo(), comando.observacao(),
                OrigemMovimentacaoEstoque.MANUAL, null, comando.usuarioResponsavel());
        return movimentacaoEstoqueRepositoryPort.salvar(movimentacao);
    }

    private SaldoEstoque buscarOuCriarSaldo(UUID produtoId) {
        return saldoEstoqueRepositoryPort.buscarPorProdutoId(produtoId).orElseGet(() -> SaldoEstoque.novo(produtoId));
    }

    private SaldoEstoque buscarSaldoObrigatorio(UUID produtoId) {
        return saldoEstoqueRepositoryPort.buscarPorProdutoId(produtoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Saldo de estoque inconsistente: produto %s possui reserva sem saldo registrado"
                                .formatted(produtoId)));
    }

    private List<MovimentacaoEstoque> reservasDoPedido(UUID pedidoId) {
        return movimentacaoEstoqueRepositoryPort.listarPorOrigemOperacaoId(pedidoId).stream()
                .filter(movimentacao -> movimentacao.getTipo() == TipoMovimentacaoEstoque.RESERVA)
                .toList();
    }

    private boolean jaProcessadoAposReserva(UUID pedidoId) {
        return movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(
                pedidoId, TipoMovimentacaoEstoque.LIBERACAO_RESERVA)
                || movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(
                pedidoId, TipoMovimentacaoEstoque.SAIDA);
    }
}
