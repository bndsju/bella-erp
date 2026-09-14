package com.bella.backend.application.orcamento.usecase;

import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.Endereco;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.orcamento.port.in.ComandoItemOrcamento;
import com.bella.backend.domain.orcamento.port.in.ComandoOrcamento;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase.FiltroListagem;
import com.bella.backend.domain.orcamento.port.out.OrcamentoRepositoryPort;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
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
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepositoryPort orcamentoRepositoryPort;

    @Mock
    private BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    @Mock
    private BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    private OrcamentoService orcamentoService;

    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID PRODUTO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orcamentoService = new OrcamentoService(orcamentoRepositoryPort, buscarClientePorIdUseCase,
                buscarProdutoPorIdUseCase);
    }

    private ComandoOrcamento comandoValido() {
        return new ComandoOrcamento(
                CLIENTE_ID,
                List.of(new ComandoItemOrcamento(PRODUTO_ID, new BigDecimal("2"), new BigDecimal("50.00"))),
                new BigDecimal("10"),
                new BigDecimal("15.00"),
                LocalDate.now().plusDays(7),
                "À vista",
                null);
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
    void cadastraOrcamentoQuandoClienteEProdutoExistem() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(clienteFake());
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_ID)).thenReturn(produtoFake());
        when(orcamentoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Orcamento orcamento = orcamentoService.cadastrar(comandoValido());

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.RASCUNHO);
        assertThat(orcamento.getSubtotal()).isEqualByComparingTo("100.00");
        verify(orcamentoRepositoryPort).salvar(any());
    }

    @Test
    void rejeitaCadastroComClienteInexistente() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Cliente", CLIENTE_ID));

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.cadastrar(comandoValido()));
        verify(orcamentoRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaCadastroComProdutoInexistente() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(clienteFake());
        when(buscarProdutoPorIdUseCase.buscarPorId(PRODUTO_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Produto", PRODUTO_ID));

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.cadastrar(comandoValido()));
        verify(orcamentoRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(orcamentoRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> orcamentoService.buscarPorId(id));
    }

    @Test
    void buscarPorIdPersisteExpiracaoQuandoPrazoJaPassou() {
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        Orcamento orcamentoVencido = Orcamento.existente(id, CLIENTE_ID,
                List.of(com.bella.backend.domain.orcamento.model.OrcamentoItem.novo(
                        PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().minusDays(1), "À vista", null,
                StatusOrcamento.ENVIADO, agora, agora);

        when(orcamentoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(orcamentoVencido));
        when(orcamentoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Orcamento resultado = orcamentoService.buscarPorId(id);

        assertThat(resultado.getStatus()).isEqualTo(StatusOrcamento.EXPIRADO);
        verify(orcamentoRepositoryPort).salvar(any());
    }

    @Test
    void enviarAprovarFluxoCompleto() {
        UUID id = UUID.randomUUID();
        Orcamento rascunho = Orcamento.novo(CLIENTE_ID,
                List.of(com.bella.backend.domain.orcamento.model.OrcamentoItem.novo(
                        PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().plusDays(1), "À vista", null);

        when(orcamentoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(rascunho));
        when(orcamentoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Orcamento enviado = orcamentoService.enviar(id);
        assertThat(enviado.getStatus()).isEqualTo(StatusOrcamento.ENVIADO);

        Orcamento aprovado = orcamentoService.aprovar(id);
        assertThat(aprovado.getStatus()).isEqualTo(StatusOrcamento.APROVADO);
    }

    @Test
    void cancelarPropagaRegraDeNegocioQuandoEstadoFinal() {
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        Orcamento aprovado = Orcamento.existente(id, CLIENTE_ID,
                List.of(com.bella.backend.domain.orcamento.model.OrcamentoItem.novo(
                        PRODUTO_ID, BigDecimal.ONE, BigDecimal.TEN)),
                BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now().plusDays(1), "À vista", null,
                StatusOrcamento.APROVADO, agora, agora);

        when(orcamentoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(aprovado));

        assertThrows(RegraDeNegocioException.class, () -> orcamentoService.cancelar(id));
    }

    @Test
    void listarDelegaParaRepositorio() {
        FiltroListagem filtro = new FiltroListagem(CLIENTE_ID, StatusOrcamento.RASCUNHO);
        when(orcamentoRepositoryPort.listar(filtro)).thenReturn(List.of());

        List<Orcamento> resultado = orcamentoService.listar(filtro);

        assertThat(resultado).isEmpty();
    }
}
