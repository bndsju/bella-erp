package com.bella.backend.application.compra.usecase;

import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.compra.model.CompraItem;
import com.bella.backend.domain.compra.model.StatusCompra;
import com.bella.backend.domain.compra.port.in.ComandoCompra;
import com.bella.backend.domain.compra.port.in.ComandoItemCompra;
import com.bella.backend.domain.compra.port.out.CompraRepositoryPort;
import com.bella.backend.domain.estoque.port.in.ItemEntradaCompra;
import com.bella.backend.domain.estoque.port.in.RegistrarEntradasDeCompraUseCase;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepositoryPort compraRepositoryPort;

    @Mock
    private BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;

    @Mock
    private BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    @Mock
    private RegistrarEntradasDeCompraUseCase registrarEntradasDeCompraUseCase;

    private CompraService compraService;

    private static final UUID FORNECEDOR_ID = UUID.randomUUID();
    private static final UUID PRODUTO_A = UUID.randomUUID();
    private static final UUID PRODUTO_B = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        compraService = new CompraService(compraRepositoryPort, buscarFornecedorPorIdUseCase,
                buscarProdutoPorIdUseCase, registrarEntradasDeCompraUseCase);
    }

    private Fornecedor fornecedorAtivo() {
        return Fornecedor.novo("Distribuidora Alfa", "1133334444");
    }

    private Produto produtoAtivo() {
        return Produto.novo("SKU-001", null, "Arroz", null, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10.00"), new BigDecimal("20.00"), BigDecimal.ZERO);
    }

    private ComandoCompra comandoValido() {
        return new ComandoCompra(FORNECEDOR_ID, LocalDate.now().plusDays(5),
                List.of(new ComandoItemCompra(PRODUTO_A, new BigDecimal("10"), new BigDecimal("5.00"), null),
                        new ComandoItemCompra(PRODUTO_B, new BigDecimal("4"), new BigDecimal("2.50"), null)),
                null, new BigDecimal("10.00"), null, "30 dias", null);
    }

    private Compra compraConfirmada() {
        Compra compra = Compra.nova(FORNECEDOR_ID, null,
                List.of(CompraItem.novo(PRODUTO_A, new BigDecimal("10"), new BigDecimal("5.00"), null),
                        CompraItem.novo(PRODUTO_B, new BigDecimal("4"), new BigDecimal("2.50"), null)),
                null, null, null, "À vista", null);
        compra.confirmar();
        return compra;
    }

    @Test
    void cadastraCompraValidaCalculandoTotais() {
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID)).thenReturn(fornecedorAtivo());
        when(buscarProdutoPorIdUseCase.buscarPorId(any())).thenReturn(produtoAtivo());
        when(compraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Compra compra = compraService.cadastrar(comandoValido());

        assertThat(compra.getStatus()).isEqualTo(StatusCompra.RASCUNHO);
        assertThat(compra.getValorProdutos()).isEqualByComparingTo("60.00");
        assertThat(compra.getValorTotal()).isEqualByComparingTo("70.00");
    }

    @Test
    void rejeitaCompraSemFornecedor() {
        ComandoCompra comando = new ComandoCompra(null, null,
                List.of(new ComandoItemCompra(PRODUTO_A, BigDecimal.ONE, BigDecimal.TEN, null)),
                null, null, null, "À vista", null);

        assertThrows(RegraDeNegocioException.class, () -> compraService.cadastrar(comando));
        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaCompraSemItens() {
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID)).thenReturn(fornecedorAtivo());
        ComandoCompra comando = new ComandoCompra(FORNECEDOR_ID, null, List.of(), null, null, null, "À vista", null);

        assertThrows(RegraDeNegocioException.class, () -> compraService.cadastrar(comando));
        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaFornecedorInativo() {
        Fornecedor inativo = fornecedorAtivo();
        inativo.inativar();
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID)).thenReturn(inativo);

        assertThrows(RegraDeNegocioException.class, () -> compraService.cadastrar(comandoValido()));
        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaProdutoInativo() {
        Produto inativo = produtoAtivo();
        inativo.inativar();
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID)).thenReturn(fornecedorAtivo());
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_A)).thenReturn(inativo);

        assertThrows(RegraDeNegocioException.class, () -> compraService.cadastrar(comandoValido()));
        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(compraRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> compraService.buscarPorId(id));
    }

    @Test
    void confirmaCompra() {
        Compra rascunho = Compra.nova(FORNECEDOR_ID, null,
                List.of(CompraItem.novo(PRODUTO_A, BigDecimal.ONE, BigDecimal.TEN, null)),
                null, null, null, "À vista", null);
        when(compraRepositoryPort.buscarPorId(rascunho.getId())).thenReturn(Optional.of(rascunho));
        when(compraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(compraService.confirmar(rascunho.getId()).getStatus()).isEqualTo(StatusCompra.CONFIRMADA);
        verify(registrarEntradasDeCompraUseCase, never()).registrarEntradasDeCompra(any(), any());
    }

    @Test
    void recebimentoGeraUmaEntradaPorItemEMarcaRecebida() {
        Compra compra = compraConfirmada();
        when(compraRepositoryPort.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        when(compraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Compra recebida = compraService.receber(compra.getId(), null);

        assertThat(recebida.getStatus()).isEqualTo(StatusCompra.RECEBIDA);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ItemEntradaCompra>> captor = ArgumentCaptor.forClass(List.class);
        verify(registrarEntradasDeCompraUseCase).registrarEntradasDeCompra(any(UUID.class), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).extracting(ItemEntradaCompra::produtoId)
                .containsExactly(PRODUTO_A, PRODUTO_B);
        assertThat(captor.getValue()).extracting(ItemEntradaCompra::quantidade)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("10"), new BigDecimal("4"));
        assertThat(captor.getValue()).extracting(ItemEntradaCompra::custoUnitario)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(new BigDecimal("5.00"), new BigDecimal("2.50"));
    }

    @Test
    void falhaNoEstoqueImpedeSalvarCompraComoRecebida() {
        Compra compra = compraConfirmada();
        when(compraRepositoryPort.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        doThrow(new RegraDeNegocioException("Falha ao registrar entrada"))
                .when(registrarEntradasDeCompraUseCase).registrarEntradasDeCompra(any(), any());

        assertThrows(RegraDeNegocioException.class, () -> compraService.receber(compra.getId(), null));

        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void segundoRecebimentoNaoDuplicaEstoque() {
        Compra compra = compraConfirmada();
        when(compraRepositoryPort.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        when(compraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        compraService.receber(compra.getId(), null);
        assertThrows(RegraDeNegocioException.class, () -> compraService.receber(compra.getId(), null));

        verify(registrarEntradasDeCompraUseCase, org.mockito.Mockito.times(1))
                .registrarEntradasDeCompra(any(), any());
    }

    @Test
    void compraRecebidaNaoPodeSerCancelada() {
        Compra compra = compraConfirmada();
        compra.receber(null);
        when(compraRepositoryPort.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));

        assertThrows(RegraDeNegocioException.class, () -> compraService.cancelar(compra.getId(), "Tarde demais"));
        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void compraRecebidaNaoPodeSerEditada() {
        Compra compra = compraConfirmada();
        compra.receber(null);
        when(compraRepositoryPort.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID)).thenReturn(fornecedorAtivo());
        when(buscarProdutoPorIdUseCase.buscarPorId(any())).thenReturn(produtoAtivo());

        assertThrows(RegraDeNegocioException.class, () -> compraService.editar(compra.getId(), comandoValido()));
        verify(compraRepositoryPort, never()).salvar(any());
    }

    @Test
    void compraCanceladaNaoGeraEstoque() {
        Compra compra = compraConfirmada();
        when(compraRepositoryPort.buscarPorId(compra.getId())).thenReturn(Optional.of(compra));
        when(compraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Compra cancelada = compraService.cancelar(compra.getId(), "Fornecedor sem estoque");

        assertThat(cancelada.getStatus()).isEqualTo(StatusCompra.CANCELADA);
        assertThrows(RegraDeNegocioException.class, () -> compraService.receber(compra.getId(), null));
        verify(registrarEntradasDeCompraUseCase, never()).registrarEntradasDeCompra(any(), any());
    }

    @Test
    void pendentesDeRecebimentoFiltraPorConfirmadas() {
        when(compraRepositoryPort.listar(any())).thenReturn(List.of());

        assertThat(compraService.listarPendentesDeRecebimento()).isEmpty();
        verify(compraRepositoryPort).listar(
                new com.bella.backend.domain.compra.port.in.ListarComprasUseCase.FiltroListagem(
                        null, StatusCompra.CONFIRMADA, null, null));
    }
}
