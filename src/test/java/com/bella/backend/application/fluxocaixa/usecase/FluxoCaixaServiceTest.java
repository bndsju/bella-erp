package com.bella.backend.application.fluxocaixa.usecase;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.in.BuscarCategoriaFluxoCaixaPorIdUseCase;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCategoriaCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ComandoLancamentoManualCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ComandoMovimentacaoAutomaticaCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ListarMovimentacoesCaixaUseCase.FiltroListagem;
import com.bella.backend.domain.fluxocaixa.port.out.MovimentacaoCaixaRepositoryPort;
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
class FluxoCaixaServiceTest {

    @Mock
    private MovimentacaoCaixaRepositoryPort movimentacaoCaixaRepositoryPort;

    @Mock
    private BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase;

    private FluxoCaixaService fluxoCaixaService;

    private static final UUID CATEGORIA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        fluxoCaixaService = new FluxoCaixaService(movimentacaoCaixaRepositoryPort,
                buscarCategoriaFluxoCaixaPorIdUseCase);
    }

    private ComandoLancamentoManualCaixa lancamentoPrevisto() {
        return new ComandoLancamentoManualCaixa(TipoMovimentacaoCaixa.ENTRADA, "Receita avulsa", CATEGORIA_ID,
                new BigDecimal("500.00"), SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now().plusDays(5), null,
                null);
    }

    private ComandoMovimentacaoAutomaticaCaixa automatica(UUID referencia) {
        return new ComandoMovimentacaoAutomaticaCaixa(TipoMovimentacaoCaixa.ENTRADA, "Parcela 1", null,
                new BigDecimal("500.00"), SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now().plusDays(5), null,
                OrigemMovimentacaoCaixa.CONTA_RECEBER, referencia, null);
    }

    private MovimentacaoCaixa mov(TipoMovimentacaoCaixa tipo, String valor, SituacaoMovimentacaoCaixa situacao,
                                   LocalDate data, UUID categoriaId) {
        return MovimentacaoCaixa.novaManual(tipo, "Mov", categoriaId == null ? CATEGORIA_ID : categoriaId,
                new BigDecimal(valor), situacao, data, situacao == SituacaoMovimentacaoCaixa.REALIZADO ? data : null,
                null);
    }

    @Test
    void lancaMovimentacaoManualComCategoriaAtiva() {
        when(buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(CATEGORIA_ID))
                .thenReturn(CategoriaFluxoCaixa.novo("Receitas"));
        when(movimentacaoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoCaixa movimentacao = fluxoCaixaService.lancar(lancamentoPrevisto());

        assertThat(movimentacao.getOrigem()).isEqualTo(OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL);
        assertThat(movimentacao.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.PREVISTO);
    }

    @Test
    void rejeitaLancamentoComCategoriaInativa() {
        CategoriaFluxoCaixa inativa = CategoriaFluxoCaixa.novo("Antiga");
        inativa.inativar();
        when(buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(CATEGORIA_ID)).thenReturn(inativa);

        assertThrows(RegraDeNegocioException.class, () -> fluxoCaixaService.lancar(lancamentoPrevisto()));
        verify(movimentacaoCaixaRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaLancamentoComCategoriaInexistente() {
        when(buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(CATEGORIA_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Categoria", CATEGORIA_ID));

        assertThrows(EntidadeNaoEncontradaException.class, () -> fluxoCaixaService.lancar(lancamentoPrevisto()));
    }

    @Test
    void realizaLancamentoManualPrevisto() {
        MovimentacaoCaixa movimentacao = mov(TipoMovimentacaoCaixa.ENTRADA, "100.00",
                SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now().plusDays(2), null);
        when(movimentacaoCaixaRepositoryPort.buscarPorId(movimentacao.getId())).thenReturn(Optional.of(movimentacao));
        when(movimentacaoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoCaixa realizada = fluxoCaixaService.realizar(movimentacao.getId(), null);

        assertThat(realizada.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.REALIZADO);
        assertThat(realizada.getDataEfetiva()).isEqualTo(LocalDate.now());
    }

    @Test
    void movimentacaoAutomaticaNaoPodeSerRealizadaNemCanceladaPelosEndpointsManuais() {
        MovimentacaoCaixa automatica = MovimentacaoCaixa.novaAutomatica(TipoMovimentacaoCaixa.ENTRADA, "Parcela",
                null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null,
                OrigemMovimentacaoCaixa.CONTA_RECEBER, UUID.randomUUID(), null);
        when(movimentacaoCaixaRepositoryPort.buscarPorId(automatica.getId())).thenReturn(Optional.of(automatica));

        assertThrows(RegraDeNegocioException.class, () -> fluxoCaixaService.realizar(automatica.getId(), null));
        assertThrows(RegraDeNegocioException.class, () -> fluxoCaixaService.cancelar(automatica.getId(), "Motivo"));
        verify(movimentacaoCaixaRepositoryPort, never()).salvar(any());
    }

    @Test
    void cancelaLancamentoManual() {
        MovimentacaoCaixa movimentacao = mov(TipoMovimentacaoCaixa.SAIDA, "100.00",
                SituacaoMovimentacaoCaixa.REALIZADO, LocalDate.now(), null);
        when(movimentacaoCaixaRepositoryPort.buscarPorId(movimentacao.getId())).thenReturn(Optional.of(movimentacao));
        when(movimentacaoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(fluxoCaixaService.cancelar(movimentacao.getId(), "Estorno").isCancelada()).isTrue();
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(movimentacaoCaixaRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> fluxoCaixaService.buscarPorId(id));
    }

    @Test
    void registraMovimentacaoAutomaticaPrevista() {
        UUID referencia = UUID.randomUUID();
        when(movimentacaoCaixaRepositoryPort.buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa.CONTA_RECEBER,
                referencia)).thenReturn(Optional.empty());
        when(movimentacaoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoCaixa movimentacao = fluxoCaixaService.registrar(automatica(referencia));

        assertThat(movimentacao.getOrigemReferenciaId()).isEqualTo(referencia);
        assertThat(movimentacao.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.PREVISTO);
    }

    @Test
    void naoDuplicaMovimentacaoDaMesmaOrigem() {
        UUID referencia = UUID.randomUUID();
        MovimentacaoCaixa existente = MovimentacaoCaixa.novaAutomatica(TipoMovimentacaoCaixa.ENTRADA, "Parcela",
                null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null,
                OrigemMovimentacaoCaixa.CONTA_RECEBER, referencia, null);
        when(movimentacaoCaixaRepositoryPort.buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa.CONTA_RECEBER,
                referencia)).thenReturn(Optional.of(existente));

        assertThrows(RegraDeNegocioException.class, () -> fluxoCaixaService.registrar(automatica(referencia)));
        verify(movimentacaoCaixaRepositoryPort, never()).salvar(any());
    }

    @Test
    void movimentacaoAutomaticaSemReferenciaEhRejeitada() {
        assertThrows(RegraDeNegocioException.class, () -> fluxoCaixaService.registrar(automatica(null)));
        verify(movimentacaoCaixaRepositoryPort, never()).salvar(any());
    }

    @Test
    void efetivaAMovimentacaoDaOrigemSemCriarOutra() {
        UUID referencia = UUID.randomUUID();
        MovimentacaoCaixa existente = MovimentacaoCaixa.novaAutomatica(TipoMovimentacaoCaixa.SAIDA, "Parcela",
                null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null,
                OrigemMovimentacaoCaixa.CONTA_PAGAR, referencia, null);
        when(movimentacaoCaixaRepositoryPort.buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa.CONTA_PAGAR,
                referencia)).thenReturn(Optional.of(existente));
        when(movimentacaoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MovimentacaoCaixa realizada = fluxoCaixaService.realizar(OrigemMovimentacaoCaixa.CONTA_PAGAR, referencia,
                LocalDate.now());

        assertThat(realizada.getId()).isEqualTo(existente.getId());
        assertThat(realizada.getSituacao()).isEqualTo(SituacaoMovimentacaoCaixa.REALIZADO);
    }

    @Test
    void cancelaMovimentacaoQuandoARegistroDeOrigemEhCancelado() {
        UUID referencia = UUID.randomUUID();
        MovimentacaoCaixa existente = MovimentacaoCaixa.novaAutomatica(TipoMovimentacaoCaixa.ENTRADA, "Parcela",
                null, BigDecimal.TEN, SituacaoMovimentacaoCaixa.PREVISTO, LocalDate.now(), null,
                OrigemMovimentacaoCaixa.CONTA_RECEBER, referencia, null);
        when(movimentacaoCaixaRepositoryPort.buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa.CONTA_RECEBER,
                referencia)).thenReturn(Optional.of(existente));
        when(movimentacaoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(fluxoCaixaService.cancelar(OrigemMovimentacaoCaixa.CONTA_RECEBER, referencia, "Conta cancelada")
                .isCancelada()).isTrue();
    }

    @Test
    void realizarOrigemInexistenteLancaExcecao() {
        UUID referencia = UUID.randomUUID();
        when(movimentacaoCaixaRepositoryPort.buscarPorOrigemEReferencia(OrigemMovimentacaoCaixa.CONTA_RECEBER,
                referencia)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> fluxoCaixaService.realizar(
                OrigemMovimentacaoCaixa.CONTA_RECEBER, referencia, LocalDate.now()));
    }

    @Test
    void listaFiltrandoPeriodoPelaDataDeReferenciaEOrdenandoDaMaisRecente() {
        LocalDate hoje = LocalDate.now();
        MovimentacaoCaixa antiga = mov(TipoMovimentacaoCaixa.ENTRADA, "10.00", SituacaoMovimentacaoCaixa.REALIZADO,
                hoje.minusDays(30), null);
        MovimentacaoCaixa recente = mov(TipoMovimentacaoCaixa.ENTRADA, "20.00", SituacaoMovimentacaoCaixa.REALIZADO,
                hoje.minusDays(2), null);
        MovimentacaoCaixa futura = mov(TipoMovimentacaoCaixa.ENTRADA, "30.00", SituacaoMovimentacaoCaixa.PREVISTO,
                hoje.plusDays(10), null);
        when(movimentacaoCaixaRepositoryPort.listar(null, null, null, null, false))
                .thenReturn(List.of(antiga, recente, futura));

        List<MovimentacaoCaixa> todas = fluxoCaixaService.listar(
                new FiltroListagem(null, null, null, null, null, null, false));
        List<MovimentacaoCaixa> recentes = fluxoCaixaService.listar(
                new FiltroListagem(null, null, null, null, hoje.minusDays(5), hoje.plusDays(15), false));

        assertThat(todas).containsExactly(futura, recente, antiga);
        assertThat(recentes).containsExactly(futura, recente);
    }

    @Test
    void resumoDoPeriodoSeparaPrevistoERealizado() {
        LocalDate hoje = LocalDate.now();
        when(movimentacaoCaixaRepositoryPort.listar(null, null, null, null, false)).thenReturn(List.of(
                mov(TipoMovimentacaoCaixa.ENTRADA, "1000.00", SituacaoMovimentacaoCaixa.REALIZADO, hoje, null),
                mov(TipoMovimentacaoCaixa.SAIDA, "400.00", SituacaoMovimentacaoCaixa.REALIZADO, hoje, null),
                mov(TipoMovimentacaoCaixa.ENTRADA, "300.00", SituacaoMovimentacaoCaixa.PREVISTO,
                        hoje.plusDays(3), null),
                mov(TipoMovimentacaoCaixa.SAIDA, "100.00", SituacaoMovimentacaoCaixa.PREVISTO,
                        hoje.plusDays(3), null),
                mov(TipoMovimentacaoCaixa.ENTRADA, "9999.00", SituacaoMovimentacaoCaixa.REALIZADO,
                        hoje.minusDays(90), null)));

        ResumoCaixa resumo = fluxoCaixaService.consultarResumo(hoje.minusDays(1), hoje.plusDays(5));

        assertThat(resumo.saldoRealizado()).isEqualByComparingTo("600.00");
        assertThat(resumo.saldoProjetado()).isEqualByComparingTo("800.00");
    }

    @Test
    void saldoAcumuladoConsideraTudoAteAData() {
        LocalDate hoje = LocalDate.now();
        when(movimentacaoCaixaRepositoryPort.listar(null, null, null, null, false)).thenReturn(List.of(
                mov(TipoMovimentacaoCaixa.ENTRADA, "1000.00", SituacaoMovimentacaoCaixa.REALIZADO,
                        hoje.minusDays(60), null),
                mov(TipoMovimentacaoCaixa.SAIDA, "250.00", SituacaoMovimentacaoCaixa.REALIZADO,
                        hoje.minusDays(10), null),
                mov(TipoMovimentacaoCaixa.ENTRADA, "500.00", SituacaoMovimentacaoCaixa.PREVISTO,
                        hoje.plusDays(20), null)));

        assertThat(fluxoCaixaService.consultarSaldoAcumulado(null).saldoRealizado()).isEqualByComparingTo("750.00");
        assertThat(fluxoCaixaService.consultarSaldoAcumulado(null).saldoProjetado()).isEqualByComparingTo("750.00");
        assertThat(fluxoCaixaService.consultarSaldoAcumulado(hoje.plusDays(30)).saldoProjetado())
                .isEqualByComparingTo("1250.00");
        assertThat(fluxoCaixaService.consultarSaldoAcumulado(hoje.minusDays(30)).saldoRealizado())
                .isEqualByComparingTo("1000.00");
    }

    @Test
    void resumoPorCategoriaAgrupaIncluindoMovimentacoesSemCategoria() {
        LocalDate hoje = LocalDate.now();
        UUID outraCategoria = UUID.randomUUID();
        MovimentacaoCaixa semCategoria = MovimentacaoCaixa.novaAutomatica(TipoMovimentacaoCaixa.ENTRADA, "Parcela",
                null, new BigDecimal("70.00"), SituacaoMovimentacaoCaixa.PREVISTO, hoje, null,
                OrigemMovimentacaoCaixa.CONTA_RECEBER, UUID.randomUUID(), null);
        when(movimentacaoCaixaRepositoryPort.listar(null, null, null, null, false)).thenReturn(List.of(
                mov(TipoMovimentacaoCaixa.ENTRADA, "100.00", SituacaoMovimentacaoCaixa.REALIZADO, hoje, CATEGORIA_ID),
                mov(TipoMovimentacaoCaixa.SAIDA, "40.00", SituacaoMovimentacaoCaixa.REALIZADO, hoje, CATEGORIA_ID),
                mov(TipoMovimentacaoCaixa.SAIDA, "15.00", SituacaoMovimentacaoCaixa.REALIZADO, hoje, outraCategoria),
                semCategoria));

        List<ResumoCategoriaCaixa> resultado = fluxoCaixaService.consultarResumoPorCategoria(null, null);

        assertThat(resultado).hasSize(3);
        assertThat(resultado).filteredOn(item -> CATEGORIA_ID.equals(item.categoriaId()))
                .singleElement().satisfies(item -> assertThat(item.resumo().saldoRealizado())
                        .isEqualByComparingTo("60.00"));
        assertThat(resultado).filteredOn(item -> item.categoriaId() == null)
                .singleElement().satisfies(item -> assertThat(item.resumo().entradasPrevistas())
                        .isEqualByComparingTo("70.00"));
    }
}
