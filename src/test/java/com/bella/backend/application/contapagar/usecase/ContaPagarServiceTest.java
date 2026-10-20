package com.bella.backend.application.contapagar.usecase;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.port.in.BuscarCategoriaDespesaPorIdUseCase;
import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.DadosParcela;
import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoPagamentoContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoParcelaContaPagar;
import com.bella.backend.domain.contapagar.port.in.ListarContasPagarUseCase.FiltroListagem;
import com.bella.backend.domain.contapagar.port.out.ContaPagarRepositoryPort;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaPagarServiceTest {

    @Mock
    private ContaPagarRepositoryPort contaPagarRepositoryPort;

    @Mock
    private BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;

    @Mock
    private BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase;

    private ContaPagarService contaPagarService;

    private static final UUID FORNECEDOR_ID = UUID.randomUUID();
    private static final UUID CATEGORIA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        contaPagarService = new ContaPagarService(contaPagarRepositoryPort, buscarFornecedorPorIdUseCase,
                buscarCategoriaDespesaPorIdUseCase);
    }

    private ComandoContaPagar comandoUnico(UUID fornecedorId) {
        return new ComandoContaPagar("Aluguel", fornecedorId, CATEGORIA_ID, new BigDecimal("1500.00"),
                LocalDate.now().plusDays(10), null, null, OrigemContaPagar.MANUAL, null);
    }

    private ContaPagar conta(LocalDate vencimento) {
        return ContaPagar.nova("Conta", null, CATEGORIA_ID, new BigDecimal("100.00"), vencimento, null, null,
                OrigemContaPagar.MANUAL, null);
    }

    @Test
    void cadastraContaManualSemFornecedor() {
        when(buscarCategoriaDespesaPorIdUseCase.buscarPorId(CATEGORIA_ID))
                .thenReturn(CategoriaDespesa.novo("Aluguel"));
        when(contaPagarRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ContaPagar conta = contaPagarService.cadastrar(comandoUnico(null));

        assertThat(conta.getFornecedorId()).isNull();
        assertThat(conta.getOrigem()).isEqualTo(OrigemContaPagar.MANUAL);
        assertThat(conta.getParcelas()).hasSize(1);
        verify(buscarFornecedorPorIdUseCase, never()).buscarPorId(any());
    }

    @Test
    void cadastraContaParceladaComFornecedorAtivo() {
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID))
                .thenReturn(Fornecedor.novo("Distribuidora", null));
        when(buscarCategoriaDespesaPorIdUseCase.buscarPorId(CATEGORIA_ID))
                .thenReturn(CategoriaDespesa.novo("Insumos"));
        when(contaPagarRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ComandoContaPagar comando = new ComandoContaPagar("Insumos", FORNECEDOR_ID, CATEGORIA_ID,
                new BigDecimal("300.00"), null,
                List.of(new ComandoParcelaContaPagar(new BigDecimal("150.00"), LocalDate.now().plusDays(30)),
                        new ComandoParcelaContaPagar(new BigDecimal("150.00"), LocalDate.now().plusDays(60))),
                null, OrigemContaPagar.MANUAL, null);

        ContaPagar conta = contaPagarService.cadastrar(comando);

        assertThat(conta.getParcelas()).hasSize(2);
        assertThat(conta.getFornecedorId()).isEqualTo(FORNECEDOR_ID);
    }

    @Test
    void rejeitaFornecedorInativo() {
        Fornecedor inativo = Fornecedor.novo("Distribuidora", null);
        inativo.inativar();
        when(buscarFornecedorPorIdUseCase.buscarPorId(FORNECEDOR_ID)).thenReturn(inativo);

        assertThrows(RegraDeNegocioException.class, () -> contaPagarService.cadastrar(comandoUnico(FORNECEDOR_ID)));
        verify(contaPagarRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaCategoriaInativa() {
        CategoriaDespesa inativa = CategoriaDespesa.novo("Antiga");
        inativa.inativar();
        when(buscarCategoriaDespesaPorIdUseCase.buscarPorId(CATEGORIA_ID)).thenReturn(inativa);

        assertThrows(RegraDeNegocioException.class, () -> contaPagarService.cadastrar(comandoUnico(null)));
        verify(contaPagarRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaSegundaContaParaMesmaOrigemAutomatica() {
        UUID compraId = UUID.randomUUID();
        when(buscarCategoriaDespesaPorIdUseCase.buscarPorId(CATEGORIA_ID))
                .thenReturn(CategoriaDespesa.novo("Compras"));
        when(contaPagarRepositoryPort.existePorOrigemEReferencia(OrigemContaPagar.COMPRA, compraId))
                .thenReturn(true);

        ComandoContaPagar comando = new ComandoContaPagar("Compra", null, CATEGORIA_ID, BigDecimal.TEN,
                LocalDate.now(), null, null, OrigemContaPagar.COMPRA, compraId);

        assertThrows(RegraDeNegocioException.class, () -> contaPagarService.cadastrar(comando));
        verify(contaPagarRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(contaPagarRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> contaPagarService.buscarPorId(id));
    }

    @Test
    void registraPagamentoUsandoDataDeHojeQuandoNaoInformada() {
        ContaPagar conta = conta(LocalDate.now().plusDays(5));
        when(contaPagarRepositoryPort.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));
        when(contaPagarRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ContaPagar atualizada = contaPagarService.registrarPagamento(conta.getId(),
                new ComandoPagamentoContaPagar(conta.getParcelas().get(0).getId(), new BigDecimal("40.00"), null,
                        null, null, null, null));

        assertThat(atualizada.getStatus()).isEqualTo(StatusContaPagar.PARCIALMENTE_PAGA);
        assertThat(atualizada.getParcelas().get(0).getPagamentos().get(0).getDataPagamento())
                .isEqualTo(LocalDate.now());
    }

    @Test
    void pagamentoAcimaDoSaldoNaoEhSalvo() {
        ContaPagar conta = conta(LocalDate.now().plusDays(5));
        when(contaPagarRepositoryPort.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));

        assertThrows(RegraDeNegocioException.class, () -> contaPagarService.registrarPagamento(conta.getId(),
                new ComandoPagamentoContaPagar(conta.getParcelas().get(0).getId(), new BigDecimal("100.01"), null,
                        null, null, null, null)));
        verify(contaPagarRepositoryPort, never()).salvar(any());
    }

    @Test
    void cancelaContaSemPagamentos() {
        ContaPagar conta = conta(LocalDate.now().plusDays(5));
        when(contaPagarRepositoryPort.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));
        when(contaPagarRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(contaPagarService.cancelar(conta.getId(), "Duplicada").getStatus())
                .isEqualTo(StatusContaPagar.CANCELADA);
    }

    @Test
    void listaApenasVencidasEEmAbertoConformeSituacao() {
        ContaPagar atrasada = conta(LocalDate.now().minusDays(5));
        ContaPagar noPrazo = conta(LocalDate.now().plusDays(5));
        ContaPagar paga = conta(LocalDate.now().minusDays(1));
        paga.registrarPagamento(paga.getParcelas().get(0).getId(), new BigDecimal("100.00"), LocalDate.now(),
                null, null, null, null);
        when(contaPagarRepositoryPort.listar(null, null, null)).thenReturn(List.of(atrasada, noPrazo, paga));

        assertThat(contaPagarService.listar(new FiltroListagem(null, null, null, SituacaoContaPagar.VENCIDAS,
                null, null))).containsExactly(atrasada);
        assertThat(contaPagarService.listar(new FiltroListagem(null, null, null, SituacaoContaPagar.EM_ABERTO,
                null, null))).containsExactly(atrasada, noPrazo);
        assertThat(contaPagarService.listar(new FiltroListagem(null, null, null, SituacaoContaPagar.PAGAS,
                null, null))).containsExactly(paga);
    }

    @Test
    void listaFiltrandoPorPeriodoDeVencimentoDasParcelas() {
        ContaPagar cedo = conta(LocalDate.now().plusDays(2));
        ContaPagar tarde = conta(LocalDate.now().plusDays(40));
        ContaPagar parcelada = ContaPagar.nova("Parcelada", null, CATEGORIA_ID, new BigDecimal("200.00"), null,
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(1)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(35))),
                null, OrigemContaPagar.MANUAL, null);
        when(contaPagarRepositoryPort.listar(null, null, null)).thenReturn(List.of(cedo, tarde, parcelada));

        List<ContaPagar> resultado = contaPagarService.listar(new FiltroListagem(null, null, null, null,
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(45)));

        assertThat(resultado).containsExactly(tarde, parcelada);
    }

    @Test
    void listaRepassandoFiltrosSimplesAoRepositorio() {
        when(contaPagarRepositoryPort.listar(FORNECEDOR_ID, CATEGORIA_ID, StatusContaPagar.PENDENTE))
                .thenReturn(List.of());

        assertThat(contaPagarService.listar(new FiltroListagem(FORNECEDOR_ID, CATEGORIA_ID,
                StatusContaPagar.PENDENTE, null, null, null))).isEmpty();
    }
}
