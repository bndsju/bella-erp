package com.bella.backend.application.venda.usecase;

import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.PedidoItem;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.BuscarPedidoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.StatusVenda;
import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase.FiltroListagem;
import com.bella.backend.domain.venda.port.out.VendaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
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
class VendaServiceTest {

    @Mock
    private VendaRepositoryPort vendaRepositoryPort;

    @Mock
    private BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;

    private VendaService vendaService;

    private static final UUID PEDIDO_ID = UUID.randomUUID();
    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID PRODUTO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        vendaService = new VendaService(vendaRepositoryPort, buscarPedidoPorIdUseCase);
    }

    private Pedido pedidoEntregue() {
        Instant agora = Instant.now();
        return Pedido.existente(PEDIDO_ID, CLIENTE_ID, null, null,
                List.of(PedidoItem.novo(PRODUTO_ID, new BigDecimal("2"), new BigDecimal("50.00"))),
                new BigDecimal("10"), new BigDecimal("15.00"), "À vista", StatusPedido.ENTREGUE, agora, agora);
    }

    @Test
    void concluiVendaQuandoPedidoEntregueENaoConvertidoAinda() {
        when(buscarPedidoPorIdUseCase.buscarPorId(PEDIDO_ID)).thenReturn(pedidoEntregue());
        when(vendaRepositoryPort.existePorPedidoId(PEDIDO_ID)).thenReturn(false);
        when(vendaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Venda venda = vendaService.concluir(PEDIDO_ID, FormaPagamento.PIX);

        assertThat(venda.getStatus()).isEqualTo(StatusVenda.CONCLUIDA);
        assertThat(venda.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(venda.getSubtotal()).isEqualByComparingTo("100.00");
        verify(vendaRepositoryPort).salvar(any());
    }

    @Test
    void rejeitaConclusaoQuandoPedidoNaoEstaEntregue() {
        Instant agora = Instant.now();
        Pedido pedidoCriado = Pedido.existente(PEDIDO_ID, CLIENTE_ID, null, null,
                List.of(PedidoItem.novo(PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, "À vista", StatusPedido.CRIADO, agora, agora);

        when(buscarPedidoPorIdUseCase.buscarPorId(PEDIDO_ID)).thenReturn(pedidoCriado);

        assertThrows(RegraDeNegocioException.class, () -> vendaService.concluir(PEDIDO_ID, FormaPagamento.PIX));
        verify(vendaRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaConclusaoQuandoPedidoJaFoiConvertido() {
        when(buscarPedidoPorIdUseCase.buscarPorId(PEDIDO_ID)).thenReturn(pedidoEntregue());
        when(vendaRepositoryPort.existePorPedidoId(PEDIDO_ID)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> vendaService.concluir(PEDIDO_ID, FormaPagamento.PIX));
        verify(vendaRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaConclusaoComPedidoInexistente() {
        when(buscarPedidoPorIdUseCase.buscarPorId(PEDIDO_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Pedido", PEDIDO_ID));

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> vendaService.concluir(PEDIDO_ID, FormaPagamento.PIX));
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(vendaRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> vendaService.buscarPorId(id));
    }

    @Test
    void cancelarMudaStatus() {
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        Venda venda = Venda.existente(id, PEDIDO_ID, CLIENTE_ID,
                List.of(com.bella.backend.domain.venda.model.VendaItem.novo(PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, FormaPagamento.DINHEIRO, "À vista", StatusVenda.CONCLUIDA,
                agora, agora);

        when(vendaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(venda));
        when(vendaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Venda cancelada = vendaService.cancelar(id);

        assertThat(cancelada.getStatus()).isEqualTo(StatusVenda.CANCELADA);
    }

    @Test
    void listarDelegaParaRepositorio() {
        FiltroListagem filtro = new FiltroListagem(CLIENTE_ID, StatusVenda.CONCLUIDA);
        when(vendaRepositoryPort.listar(filtro)).thenReturn(List.of());

        List<Venda> resultado = vendaService.listar(filtro);

        assertThat(resultado).isEmpty();
    }
}
