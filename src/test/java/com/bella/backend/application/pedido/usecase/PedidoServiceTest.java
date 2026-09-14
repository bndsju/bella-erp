package com.bella.backend.application.pedido.usecase;

import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.Endereco;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.OrcamentoItem;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.orcamento.port.in.BuscarOrcamentoPorIdUseCase;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.PedidoItem;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.ComandoItemPedido;
import com.bella.backend.domain.pedido.port.in.ComandoPedido;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase.FiltroListagem;
import com.bella.backend.domain.pedido.port.out.PedidoRepositoryPort;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.transportadora.port.in.BuscarTransportadoraPorIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepositoryPort pedidoRepositoryPort;

    @Mock
    private BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    @Mock
    private BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    @Mock
    private BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase;

    @Mock
    private BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase;

    private PedidoService pedidoService;

    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID PRODUTO_ID = UUID.randomUUID();
    private static final UUID TRANSPORTADORA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoRepositoryPort, buscarClientePorIdUseCase, buscarProdutoPorIdUseCase,
                buscarTransportadoraPorIdUseCase, buscarOrcamentoPorIdUseCase);
    }

    private ComandoPedido comandoValido() {
        return new ComandoPedido(
                CLIENTE_ID,
                null,
                List.of(new ComandoItemPedido(PRODUTO_ID, new BigDecimal("2"), new BigDecimal("50.00"))),
                new BigDecimal("10"),
                new BigDecimal("15.00"),
                "À vista");
    }

    private ClientePessoaFisica clienteFake() {
        Endereco endereco = new Endereco("01310-100", "Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        return ClientePessoaFisica.novo("Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, endereco, null);
    }

    private Produto produtoFake() {
        return Produto.novo("SKU-001", null, "Arroz Branco", null, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("30.00"), new BigDecimal("50.00"), BigDecimal.ZERO);
    }

    @Test
    void cadastraPedidoQuandoClienteEProdutoExistem() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(clienteFake());
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_ID)).thenReturn(produtoFake());
        when(pedidoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.cadastrar(comandoValido());

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getSubtotal()).isEqualByComparingTo("100.00");
        verify(pedidoRepositoryPort).salvar(any());
    }

    @Test
    void rejeitaCadastroComClienteInexistente() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Cliente", CLIENTE_ID));

        assertThrows(EntidadeNaoEncontradaException.class, () -> pedidoService.cadastrar(comandoValido()));
        verify(pedidoRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaCadastroComTransportadoraInexistente() {
        ComandoPedido comando = new ComandoPedido(CLIENTE_ID, TRANSPORTADORA_ID,
                List.of(new ComandoItemPedido(PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, "À vista");

        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(clienteFake());
        when(buscarTransportadoraPorIdUseCase.buscarPorId(TRANSPORTADORA_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Transportadora", TRANSPORTADORA_ID));

        assertThrows(EntidadeNaoEncontradaException.class, () -> pedidoService.cadastrar(comando));
        verify(pedidoRepositoryPort, never()).salvar(any());
    }

    @Test
    void criaPedidoAPartirDeOrcamentoAprovado() {
        UUID orcamentoId = UUID.randomUUID();
        Instant agora = Instant.now();
        Orcamento orcamento = Orcamento.existente(orcamentoId, CLIENTE_ID,
                List.of(OrcamentoItem.novo(PRODUTO_ID, new BigDecimal("2"), new BigDecimal("50.00"))),
                new BigDecimal("10"), new BigDecimal("15.00"), LocalDate.now().plusDays(1), "À vista", null,
                StatusOrcamento.APROVADO, agora, agora);

        when(buscarOrcamentoPorIdUseCase.buscarPorId(orcamentoId)).thenReturn(orcamento);
        when(pedidoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.criarAPartirDeOrcamento(orcamentoId, null);

        assertThat(pedido.getOrcamentoOrigemId()).isEqualTo(orcamentoId);
        assertThat(pedido.getClienteId()).isEqualTo(CLIENTE_ID);
        assertThat(pedido.getCondicaoPagamento()).isEqualTo("À vista");
        assertThat(pedido.getSubtotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejeitaCriarPedidoAPartirDeOrcamentoNaoAprovado() {
        UUID orcamentoId = UUID.randomUUID();
        Instant agora = Instant.now();
        Orcamento orcamento = Orcamento.existente(orcamentoId, CLIENTE_ID,
                List.of(OrcamentoItem.novo(PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().plusDays(1), "À vista", null,
                StatusOrcamento.ENVIADO, agora, agora);

        when(buscarOrcamentoPorIdUseCase.buscarPorId(orcamentoId)).thenReturn(orcamento);

        assertThrows(RegraDeNegocioException.class, () -> pedidoService.criarAPartirDeOrcamento(orcamentoId, null));
        verify(pedidoRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(pedidoRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> pedidoService.buscarPorId(id));
    }

    @Test
    void fluxoDeStatusAteEntregue() {
        UUID id = UUID.randomUUID();
        Pedido criado = Pedido.novo(CLIENTE_ID, null, null,
                List.of(PedidoItem.novo(PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, "À vista");

        when(pedidoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(criado));
        when(pedidoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(pedidoService.confirmar(id).getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(pedidoService.iniciarSeparacao(id).getStatus()).isEqualTo(StatusPedido.EM_SEPARACAO);
        assertThat(pedidoService.marcarProntoParaEntrega(id).getStatus()).isEqualTo(StatusPedido.PRONTO_PARA_ENTREGA);
        assertThat(pedidoService.entregar(id).getStatus()).isEqualTo(StatusPedido.ENTREGUE);
    }

    @Test
    void cancelarPropagaRegraDeNegocioQuandoJaEntregue() {
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        Pedido entregue = Pedido.existente(id, CLIENTE_ID, null, null,
                List.of(PedidoItem.novo(PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, "À vista", StatusPedido.ENTREGUE, agora, agora);

        when(pedidoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(entregue));

        assertThrows(RegraDeNegocioException.class, () -> pedidoService.cancelar(id));
    }

    @Test
    void listarDelegaParaRepositorio() {
        FiltroListagem filtro = new FiltroListagem(CLIENTE_ID, StatusPedido.CRIADO);
        when(pedidoRepositoryPort.listar(filtro)).thenReturn(List.of());

        List<Pedido> resultado = pedidoService.listar(filtro);

        assertThat(resultado).isEmpty();
    }
}
