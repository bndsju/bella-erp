package com.bella.backend.application.estoque.usecase;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.OrigemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;
import com.bella.backend.domain.estoque.model.SaldoEstoque;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ComandoMovimentacaoManual;
import com.bella.backend.domain.estoque.port.in.ItemMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase.FiltroListagem;
import com.bella.backend.domain.estoque.port.out.MovimentacaoEstoqueRepositoryPort;
import com.bella.backend.domain.estoque.port.out.SaldoEstoqueRepositoryPort;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private SaldoEstoqueRepositoryPort saldoEstoqueRepositoryPort;

    @Mock
    private MovimentacaoEstoqueRepositoryPort movimentacaoEstoqueRepositoryPort;

    @Mock
    private BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    @Mock
    private ListarProdutosUseCase listarProdutosUseCase;

    private EstoqueService estoqueService;

    private static final UUID PRODUTO_ID = UUID.randomUUID();
    private static final UUID PEDIDO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        estoqueService = new EstoqueService(saldoEstoqueRepositoryPort, movimentacaoEstoqueRepositoryPort,
                buscarProdutoPorIdUseCase, listarProdutosUseCase);
        lenient().when(saldoEstoqueRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(movimentacaoEstoqueRepositoryPort.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Produto produtoAtivoFake() {
        return Produto.novo("SKU-001", null, "Arroz Branco", null, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10.00"), new BigDecimal("20.00"), new BigDecimal("5"));
    }

    private ComandoMovimentacaoManual comando(BigDecimal quantidade) {
        return new ComandoMovimentacaoManual(PRODUTO_ID, quantidade, "Compra de fornecedor", null, "operador");
    }

    @Test
    void registrarEntradaAumentaEstoqueEGeraMovimentacao() {
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_ID)).thenReturn(produtoAtivoFake());
        when(saldoEstoqueRepositoryPort.buscarPorProdutoId(PRODUTO_ID)).thenReturn(Optional.empty());

        MovimentacaoEstoque movimentacao = estoqueService.registrarEntrada(comando(new BigDecimal("10")));

        assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacaoEstoque.ENTRADA);
        assertThat(movimentacao.getOrigem()).isEqualTo(OrigemMovimentacaoEstoque.MANUAL);
        verify(saldoEstoqueRepositoryPort).salvar(any());
    }

    @Test
    void registrarSaidaRejeitaQuandoInsuficiente() {
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_ID)).thenReturn(produtoAtivoFake());
        when(saldoEstoqueRepositoryPort.buscarPorProdutoId(PRODUTO_ID))
                .thenReturn(Optional.of(SaldoEstoque.novo(PRODUTO_ID)));

        assertThrows(RegraDeNegocioException.class,
                () -> estoqueService.registrarSaida(comando(new BigDecimal("1"))));
    }

    @Test
    void rejeitaMovimentacaoManualParaProdutoInativo() {
        Produto produto = produtoAtivoFake();
        produto.inativar();
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_ID)).thenReturn(produto);

        assertThrows(RegraDeNegocioException.class,
                () -> estoqueService.registrarEntrada(comando(new BigDecimal("1"))));
        verify(saldoEstoqueRepositoryPort, never()).salvar(any());
    }

    @Test
    void reservarParaPedidoRegistraUmaMovimentacaoPorItem() {
        when(movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(PEDIDO_ID, TipoMovimentacaoEstoque.RESERVA))
                .thenReturn(false);
        when(saldoEstoqueRepositoryPort.buscarPorProdutoId(PRODUTO_ID))
                .thenReturn(Optional.of(saldoComEntrada("10")));

        estoqueService.reservarParaPedido(PEDIDO_ID, List.of(new ItemMovimentacaoEstoque(PRODUTO_ID, new BigDecimal("3"))));

        verify(movimentacaoEstoqueRepositoryPort).salvar(any());
        verify(saldoEstoqueRepositoryPort).salvar(any());
    }

    @Test
    void reservarParaPedidoNaoDuplicaQuandoJaReservado() {
        when(movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(PEDIDO_ID, TipoMovimentacaoEstoque.RESERVA))
                .thenReturn(true);

        estoqueService.reservarParaPedido(PEDIDO_ID, List.of(new ItemMovimentacaoEstoque(PRODUTO_ID, new BigDecimal("3"))));

        verify(saldoEstoqueRepositoryPort, never()).buscarPorProdutoId(any());
        verify(movimentacaoEstoqueRepositoryPort, never()).salvar(any());
    }

    @Test
    void liberarReservaNaoFazNadaQuandoPedidoNuncaReservou() {
        when(movimentacaoEstoqueRepositoryPort.listarPorOrigemOperacaoId(PEDIDO_ID)).thenReturn(List.of());

        estoqueService.liberarReservaParaPedido(PEDIDO_ID);

        verify(saldoEstoqueRepositoryPort, never()).salvar(any());
        verify(movimentacaoEstoqueRepositoryPort, never()).salvar(any());
    }

    @Test
    void liberarReservaDevolveDisponibilidade() {
        MovimentacaoEstoque reserva = MovimentacaoEstoque.registrar(PRODUTO_ID, TipoMovimentacaoEstoque.RESERVA,
                new BigDecimal("3"), "Reserva de estoque referente ao pedido", null,
                OrigemMovimentacaoEstoque.PEDIDO, PEDIDO_ID, null);

        when(movimentacaoEstoqueRepositoryPort.listarPorOrigemOperacaoId(PEDIDO_ID)).thenReturn(List.of(reserva));
        when(movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(PEDIDO_ID, TipoMovimentacaoEstoque.LIBERACAO_RESERVA))
                .thenReturn(false);
        when(movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(PEDIDO_ID, TipoMovimentacaoEstoque.SAIDA))
                .thenReturn(false);
        when(saldoEstoqueRepositoryPort.buscarPorProdutoId(PRODUTO_ID))
                .thenReturn(Optional.of(saldoComReserva("10", "3")));

        estoqueService.liberarReservaParaPedido(PEDIDO_ID);

        verify(saldoEstoqueRepositoryPort).salvar(any());
        verify(movimentacaoEstoqueRepositoryPort).salvar(any());
    }

    @Test
    void consumirReservaSemReservaPreviaLancaExcecao() {
        when(movimentacaoEstoqueRepositoryPort.listarPorOrigemOperacaoId(PEDIDO_ID)).thenReturn(List.of());

        assertThrows(RegraDeNegocioException.class, () -> estoqueService.consumirReservaParaPedido(PEDIDO_ID));
    }

    @Test
    void consumirReservaBaixaEstoqueDefinitivamente() {
        MovimentacaoEstoque reserva = MovimentacaoEstoque.registrar(PRODUTO_ID, TipoMovimentacaoEstoque.RESERVA,
                new BigDecimal("3"), "Reserva de estoque referente ao pedido", null,
                OrigemMovimentacaoEstoque.PEDIDO, PEDIDO_ID, null);

        when(movimentacaoEstoqueRepositoryPort.listarPorOrigemOperacaoId(PEDIDO_ID)).thenReturn(List.of(reserva));
        when(movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(PEDIDO_ID, TipoMovimentacaoEstoque.LIBERACAO_RESERVA))
                .thenReturn(false);
        when(movimentacaoEstoqueRepositoryPort.existePorOrigemOperacaoIdETipo(PEDIDO_ID, TipoMovimentacaoEstoque.SAIDA))
                .thenReturn(false);
        when(saldoEstoqueRepositoryPort.buscarPorProdutoId(PRODUTO_ID))
                .thenReturn(Optional.of(saldoComReserva("10", "3")));

        estoqueService.consumirReservaParaPedido(PEDIDO_ID);

        verify(saldoEstoqueRepositoryPort).salvar(any());
        verify(movimentacaoEstoqueRepositoryPort).salvar(any());
    }

    @Test
    void listarAbaixoDoMinimoFiltraCorretamente() {
        Produto produtoBaixo = produtoAtivoFake();
        when(listarProdutosUseCase.listar(any())).thenReturn(List.of(produtoBaixo));
        when(saldoEstoqueRepositoryPort.listarTodos()).thenReturn(List.of());

        List<PosicaoEstoqueProduto> resultado = estoqueService.listarAbaixoDoMinimo();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).produtoId()).isEqualTo(produtoBaixo.getId());
    }

    @Test
    void listarMovimentacoesDelegaParaRepositorio() {
        FiltroListagem filtro = new FiltroListagem(PRODUTO_ID, TipoMovimentacaoEstoque.ENTRADA, null, null);
        when(movimentacaoEstoqueRepositoryPort.listar(filtro)).thenReturn(List.of());

        assertThat(estoqueService.listar(filtro)).isEmpty();
    }

    private SaldoEstoque saldoComEntrada(String quantidade) {
        SaldoEstoque saldo = SaldoEstoque.novo(PRODUTO_ID);
        saldo.registrarEntrada(new BigDecimal(quantidade));
        return saldo;
    }

    private SaldoEstoque saldoComReserva(String estoque, String reservado) {
        SaldoEstoque saldo = SaldoEstoque.novo(PRODUTO_ID);
        saldo.registrarEntrada(new BigDecimal(estoque));
        saldo.reservar(new BigDecimal(reservado));
        return saldo;
    }
}
