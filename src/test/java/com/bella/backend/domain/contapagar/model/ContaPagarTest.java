package com.bella.backend.domain.contapagar.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaPagarTest {

    private static final UUID CATEGORIA_ID = UUID.randomUUID();

    private ContaPagar contaUnica(String valor, LocalDate vencimento) {
        return ContaPagar.nova("Energia elétrica", null, CATEGORIA_ID, new BigDecimal(valor), vencimento, null,
                null, OrigemContaPagar.MANUAL, null);
    }

    private ContaPagar contaParcelada() {
        return ContaPagar.nova("Equipamento", UUID.randomUUID(), CATEGORIA_ID, new BigDecimal("300.00"), null,
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(30)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(60)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(90))),
                null, OrigemContaPagar.MANUAL, null);
    }

    private void pagar(ContaPagar conta, int indiceParcela, String valor) {
        conta.registrarPagamento(conta.getParcelas().get(indiceParcela).getId(), new BigDecimal(valor),
                LocalDate.now(), null, null, null, null);
    }

    @Test
    void criaContaUnicaComUmaParcelaPendenteSemFornecedor() {
        ContaPagar conta = contaUnica("250.00", LocalDate.now().plusDays(10));

        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PENDENTE);
        assertThat(conta.getFornecedorId()).isNull();
        assertThat(conta.getParcelas()).hasSize(1);
        assertThat(conta.getParcelas().get(0).getValor()).isEqualByComparingTo("250.00");
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("250.00");
        assertThat(conta.getDataLancamento()).isEqualTo(LocalDate.now());
    }

    @Test
    void criaContaParceladaNumerandoParcelas() {
        ContaPagar conta = contaParcelada();

        assertThat(conta.getParcelas()).extracting(ParcelaContaPagar::getNumero).containsExactly(1, 2, 3);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("300.00");
    }

    @Test
    void rejeitaSomaDeParcelasDiferenteDoValor() {
        assertThrows(RegraDeNegocioException.class, () -> ContaPagar.nova("Conta", null, CATEGORIA_ID,
                new BigDecimal("300.00"), null,
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(30)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(60))),
                null, OrigemContaPagar.MANUAL, null));
    }

    @Test
    void rejeitaValorNaoPositivoVencimentoAusenteEDescricaoCategoria() {
        assertThrows(RegraDeNegocioException.class, () -> contaUnica("0", LocalDate.now()));
        assertThrows(RegraDeNegocioException.class, () -> contaUnica("10.00", null));
        assertThrows(RegraDeNegocioException.class, () -> ContaPagar.nova(" ", null, CATEGORIA_ID,
                BigDecimal.TEN, LocalDate.now(), null, null, OrigemContaPagar.MANUAL, null));
        assertThrows(RegraDeNegocioException.class, () -> ContaPagar.nova("Conta", null, null,
                BigDecimal.TEN, LocalDate.now(), null, null, OrigemContaPagar.MANUAL, null));
    }

    @Test
    void rejeitaVencimentoEParcelasJuntos() {
        assertThrows(RegraDeNegocioException.class, () -> ContaPagar.nova("Conta", null, CATEGORIA_ID,
                new BigDecimal("100.00"), LocalDate.now().plusDays(5),
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(30))),
                null, OrigemContaPagar.MANUAL, null));
    }

    @Test
    void origemAutomaticaExigeReferencia() {
        assertThrows(RegraDeNegocioException.class, () -> ContaPagar.nova("Compra", null, CATEGORIA_ID,
                BigDecimal.TEN, LocalDate.now(), null, null, OrigemContaPagar.COMPRA, null));

        UUID compraId = UUID.randomUUID();
        ContaPagar conta = ContaPagar.nova("Compra", null, CATEGORIA_ID, BigDecimal.TEN, LocalDate.now(), null,
                null, OrigemContaPagar.COMPRA, compraId);
        assertThat(conta.getOrigemReferenciaId()).isEqualTo(compraId);
    }

    @Test
    void pagamentoParcialMantemSaldoERegistraStatus() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        pagar(conta, 0, "40.00");

        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PARCIALMENTE_PAGA);
        assertThat(conta.getParcelas().get(0).getStatus()).isEqualTo(StatusParcela.PARCIALMENTE_PAGA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("60.00");
        assertThat(conta.getValorPago()).isEqualByComparingTo("40.00");
    }

    @Test
    void parcelaQuitadaFicaPagaEContaSoFicaPagaQuandoTodasQuitadas() {
        ContaPagar conta = contaParcelada();

        pagar(conta, 0, "100.00");
        assertThat(conta.getParcelas().get(0).getStatus()).isEqualTo(StatusParcela.PAGA);
        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PARCIALMENTE_PAGA);

        pagar(conta, 1, "100.00");
        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PARCIALMENTE_PAGA);

        pagar(conta, 2, "100.00");
        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PAGA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rejeitaPagamentoAcimaDoSaldoPendente() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        pagar(conta, 0, "70.00");

        assertThrows(RegraDeNegocioException.class, () -> pagar(conta, 0, "30.01"));
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("30.00");
    }

    @Test
    void descontoAbateSaldoMasJurosEMultaNao() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        PagamentoContaPagar pagamento = conta.registrarPagamento(conta.getParcelas().get(0).getId(),
                new BigDecimal("90.00"), LocalDate.now(), new BigDecimal("3.00"), new BigDecimal("2.00"),
                new BigDecimal("10.00"), "Pagamento antecipado");

        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PAGA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(pagamento.getValorDesembolsado()).isEqualByComparingTo("95.00");
    }

    @Test
    void jurosEMultaNaoPermitemPagarMaisQueOSaldo() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        conta.registrarPagamento(conta.getParcelas().get(0).getId(), new BigDecimal("100.00"), LocalDate.now(),
                new BigDecimal("5.00"), new BigDecimal("2.00"), null, null);

        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.PAGA);
        assertThat(conta.getValorPago()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejeitaValoresNegativosNoPagamentoEDataFutura() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        UUID parcelaId = conta.getParcelas().get(0).getId();

        assertThrows(RegraDeNegocioException.class, () -> conta.registrarPagamento(parcelaId, BigDecimal.TEN,
                LocalDate.now(), new BigDecimal("-1"), null, null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarPagamento(parcelaId, BigDecimal.TEN,
                LocalDate.now(), null, new BigDecimal("-1"), null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarPagamento(parcelaId, BigDecimal.TEN,
                LocalDate.now(), null, null, new BigDecimal("-1"), null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarPagamento(parcelaId, BigDecimal.ZERO,
                LocalDate.now(), null, null, null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarPagamento(parcelaId, BigDecimal.TEN,
                LocalDate.now().plusDays(1), null, null, null, null));
    }

    @Test
    void rejeitaParcelaQueNaoPertenceAConta() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        assertThrows(RegraDeNegocioException.class, () -> conta.registrarPagamento(UUID.randomUUID(),
                BigDecimal.TEN, LocalDate.now(), null, null, null, null));
    }

    @Test
    void parcelaPagaNaoRecebeNovoPagamento() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        pagar(conta, 0, "100.00");

        assertThrows(RegraDeNegocioException.class, () -> pagar(conta, 0, "1.00"));
    }

    @Test
    void historicoDePagamentosPermaneceRegistrado() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        pagar(conta, 0, "30.00");
        pagar(conta, 0, "20.00");

        assertThat(conta.getParcelas().get(0).getPagamentos()).hasSize(2);
    }

    @Test
    void contaCanceladaNaoRecebePagamentosNemEdicoes() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        conta.cancelar("Lançamento duplicado");

        assertThat(conta.getStatus()).isEqualTo(StatusContaPagar.CANCELADA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThrows(RegraDeNegocioException.class, () -> pagar(conta, 0, "10.00"));
        assertThrows(RegraDeNegocioException.class,
                () -> conta.editarDadosBasicos("Outra", null, CATEGORIA_ID, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.cancelar("De novo"));
    }

    @Test
    void cancelamentoExigeMotivoEBloqueiaContaComPagamento() {
        ContaPagar semMotivo = contaUnica("100.00", LocalDate.now().plusDays(10));
        assertThrows(RegraDeNegocioException.class, () -> semMotivo.cancelar(" "));

        ContaPagar comPagamento = contaUnica("100.00", LocalDate.now().plusDays(10));
        pagar(comPagamento, 0, "10.00");
        assertThrows(RegraDeNegocioException.class, () -> comPagamento.cancelar("Motivo"));
    }

    @Test
    void contaComPagamentosSoPermiteEditarDadosBasicos() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        pagar(conta, 0, "10.00");

        assertThrows(RegraDeNegocioException.class, () -> conta.editar("Nova", null, CATEGORIA_ID,
                new BigDecimal("200.00"), LocalDate.now().plusDays(5), null, null));

        UUID outraCategoria = UUID.randomUUID();
        conta.editarDadosBasicos("Energia - outubro", null, outraCategoria, "ajustada");
        assertThat(conta.getDescricao()).isEqualTo("Energia - outubro");
        assertThat(conta.getCategoriaDespesaId()).isEqualTo(outraCategoria);
        assertThat(conta.getValorTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void contaSemPagamentosPodeTerValorEParcelasEditados() {
        ContaPagar conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        conta.editar("Parcelada", null, CATEGORIA_ID, new BigDecimal("200.00"), null,
                List.of(new DadosParcela(new BigDecimal("120.00"), LocalDate.now().plusDays(10)),
                        new DadosParcela(new BigDecimal("80.00"), LocalDate.now().plusDays(40))), null);

        assertThat(conta.getParcelas()).hasSize(2);
        assertThat(conta.getValorTotal()).isEqualByComparingTo("200.00");
        assertThrows(RegraDeNegocioException.class, () -> conta.editar("Errada", null, CATEGORIA_ID,
                new BigDecimal("200.00"), null,
                List.of(new DadosParcela(new BigDecimal("50.00"), LocalDate.now().plusDays(10))), null));
    }

    @Test
    void calculaVencidaPeloVencimentoESaldoPendente() {
        LocalDate hoje = LocalDate.now();
        ContaPagar atrasada = contaUnica("100.00", hoje.minusDays(3));
        ContaPagar noPrazo = contaUnica("100.00", hoje.plusDays(3));
        ContaPagar venceHoje = contaUnica("100.00", hoje);

        assertThat(atrasada.estaVencida(hoje)).isTrue();
        assertThat(noPrazo.estaVencida(hoje)).isFalse();
        assertThat(venceHoje.estaVencida(hoje)).isFalse();

        pagar(atrasada, 0, "100.00");
        assertThat(atrasada.estaVencida(hoje)).isFalse();
    }

    @Test
    void contaParcialmentePagaAtrasadaContinuaVencida() {
        LocalDate hoje = LocalDate.now();
        ContaPagar conta = contaUnica("100.00", hoje.minusDays(2));

        pagar(conta, 0, "40.00");

        assertThat(conta.estaVencida(hoje)).isTrue();
        assertThat(conta.estaEmAberto()).isTrue();
    }
}
