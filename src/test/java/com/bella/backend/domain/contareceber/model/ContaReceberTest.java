package com.bella.backend.domain.contareceber.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaReceberTest {


    private ContaReceber contaUnica(String valor, LocalDate vencimento) {
        return ContaReceber.nova("Energia elétrica", null, new BigDecimal(valor), vencimento, null,
                null, OrigemContaReceber.MANUAL, null);
    }

    private ContaReceber contaParcelada() {
        return ContaReceber.nova("Equipamento", UUID.randomUUID(), new BigDecimal("300.00"), null,
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(30)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(60)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(90))),
                null, OrigemContaReceber.MANUAL, null);
    }

    private void receber(ContaReceber conta, int indiceParcela, String valor) {
        conta.registrarRecebimento(conta.getParcelas().get(indiceParcela).getId(), new BigDecimal(valor),
                LocalDate.now(), null, null, null, null);
    }

    @Test
    void criaContaUnicaComUmaParcelaPendenteSemCliente() {
        ContaReceber conta = contaUnica("250.00", LocalDate.now().plusDays(10));

        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.PENDENTE);
        assertThat(conta.getClienteId()).isNull();
        assertThat(conta.getParcelas()).hasSize(1);
        assertThat(conta.getParcelas().get(0).getValor()).isEqualByComparingTo("250.00");
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("250.00");
        assertThat(conta.getDataLancamento()).isEqualTo(LocalDate.now());
    }

    @Test
    void criaContaParceladaNumerandoParcelas() {
        ContaReceber conta = contaParcelada();

        assertThat(conta.getParcelas()).extracting(ParcelaContaReceber::getNumero).containsExactly(1, 2, 3);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("300.00");
    }

    @Test
    void rejeitaSomaDeParcelasDiferenteDoValor() {
        assertThrows(RegraDeNegocioException.class, () -> ContaReceber.nova("Conta", null,
                new BigDecimal("300.00"), null,
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(30)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(60))),
                null, OrigemContaReceber.MANUAL, null));
    }

    @Test
    void rejeitaValorNaoPositivoVencimentoAusenteEDescricao() {
        assertThrows(RegraDeNegocioException.class, () -> contaUnica("0", LocalDate.now()));
        assertThrows(RegraDeNegocioException.class, () -> contaUnica("10.00", null));
        assertThrows(RegraDeNegocioException.class, () -> ContaReceber.nova(" ", null,
                BigDecimal.TEN, LocalDate.now(), null, null, OrigemContaReceber.MANUAL, null));
    }

    @Test
    void rejeitaVencimentoEParcelasJuntos() {
        assertThrows(RegraDeNegocioException.class, () -> ContaReceber.nova("Conta", null,
                new BigDecimal("100.00"), LocalDate.now().plusDays(5),
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(30))),
                null, OrigemContaReceber.MANUAL, null));
    }

    @Test
    void origemComercialExigeReferenciaECliente() {
        UUID vendaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        assertThrows(RegraDeNegocioException.class, () -> ContaReceber.nova("Venda", clienteId,
                BigDecimal.TEN, LocalDate.now(), null, null, OrigemContaReceber.VENDA, null));
        assertThrows(RegraDeNegocioException.class, () -> ContaReceber.nova("Venda", null,
                BigDecimal.TEN, LocalDate.now(), null, null, OrigemContaReceber.VENDA, vendaId));

        ContaReceber conta = ContaReceber.nova("Venda", clienteId, BigDecimal.TEN, LocalDate.now(), null,
                null, OrigemContaReceber.VENDA, vendaId);
        assertThat(conta.getOrigemReferenciaId()).isEqualTo(vendaId);
        assertThat(conta.getClienteId()).isEqualTo(clienteId);
    }

    @Test
    void contaManualPodeSerLancadaSemCliente() {
        assertThat(contaUnica("100.00", LocalDate.now().plusDays(5)).getClienteId()).isNull();
    }

    @Test
    void recebimentoParcialMantemSaldoERegistraStatus() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        receber(conta, 0, "40.00");

        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.PARCIALMENTE_RECEBIDA);
        assertThat(conta.getParcelas().get(0).getStatus()).isEqualTo(StatusParcela.PARCIALMENTE_RECEBIDA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("60.00");
        assertThat(conta.getValorRecebido()).isEqualByComparingTo("40.00");
    }

    @Test
    void parcelaQuitadaFicaPagaEContaSoFicaPagaQuandoTodasQuitadas() {
        ContaReceber conta = contaParcelada();

        receber(conta, 0, "100.00");
        assertThat(conta.getParcelas().get(0).getStatus()).isEqualTo(StatusParcela.RECEBIDA);
        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.PARCIALMENTE_RECEBIDA);

        receber(conta, 1, "100.00");
        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.PARCIALMENTE_RECEBIDA);

        receber(conta, 2, "100.00");
        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.RECEBIDA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rejeitaRecebimentoAcimaDoSaldoPendente() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        receber(conta, 0, "70.00");

        assertThrows(RegraDeNegocioException.class, () -> receber(conta, 0, "30.01"));
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo("30.00");
    }

    @Test
    void descontoAbateSaldoMasJurosEMultaNao() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        RecebimentoContaReceber recebimento = conta.registrarRecebimento(conta.getParcelas().get(0).getId(),
                new BigDecimal("90.00"), LocalDate.now(), new BigDecimal("3.00"), new BigDecimal("2.00"),
                new BigDecimal("10.00"), "Recebimento antecipado");

        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.RECEBIDA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(recebimento.getValorTotalRecebido()).isEqualByComparingTo("95.00");
    }

    @Test
    void jurosEMultaNaoPermitemPagarMaisQueOSaldo() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        conta.registrarRecebimento(conta.getParcelas().get(0).getId(), new BigDecimal("100.00"), LocalDate.now(),
                new BigDecimal("5.00"), new BigDecimal("2.00"), null, null);

        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.RECEBIDA);
        assertThat(conta.getValorRecebido()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejeitaValoresNegativosNoRecebimentoEDataFutura() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        UUID parcelaId = conta.getParcelas().get(0).getId();

        assertThrows(RegraDeNegocioException.class, () -> conta.registrarRecebimento(parcelaId, BigDecimal.TEN,
                LocalDate.now(), new BigDecimal("-1"), null, null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarRecebimento(parcelaId, BigDecimal.TEN,
                LocalDate.now(), null, new BigDecimal("-1"), null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarRecebimento(parcelaId, BigDecimal.TEN,
                LocalDate.now(), null, null, new BigDecimal("-1"), null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarRecebimento(parcelaId, BigDecimal.ZERO,
                LocalDate.now(), null, null, null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.registrarRecebimento(parcelaId, BigDecimal.TEN,
                LocalDate.now().plusDays(1), null, null, null, null));
    }

    @Test
    void rejeitaParcelaQueNaoPertenceAConta() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        assertThrows(RegraDeNegocioException.class, () -> conta.registrarRecebimento(UUID.randomUUID(),
                BigDecimal.TEN, LocalDate.now(), null, null, null, null));
    }

    @Test
    void parcelaPagaNaoRecebeNovoRecebimento() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        receber(conta, 0, "100.00");

        assertThrows(RegraDeNegocioException.class, () -> receber(conta, 0, "1.00"));
    }

    @Test
    void historicoDeRecebimentosPermaneceRegistrado() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        receber(conta, 0, "30.00");
        receber(conta, 0, "20.00");

        assertThat(conta.getParcelas().get(0).getRecebimentos()).hasSize(2);
    }

    @Test
    void contaCanceladaNaoRecebeRecebimentosNemEdicoes() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        conta.cancelar("Lançamento duplicado");

        assertThat(conta.getStatus()).isEqualTo(StatusContaReceber.CANCELADA);
        assertThat(conta.getSaldoPendente()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThrows(RegraDeNegocioException.class, () -> receber(conta, 0, "10.00"));
        assertThrows(RegraDeNegocioException.class,
                () -> conta.editarDadosBasicos("Outra", null, null));
        assertThrows(RegraDeNegocioException.class, () -> conta.cancelar("De novo"));
    }

    @Test
    void cancelamentoExigeMotivoEBloqueiaContaComRecebimento() {
        ContaReceber semMotivo = contaUnica("100.00", LocalDate.now().plusDays(10));
        assertThrows(RegraDeNegocioException.class, () -> semMotivo.cancelar(" "));

        ContaReceber comRecebimento = contaUnica("100.00", LocalDate.now().plusDays(10));
        receber(comRecebimento, 0, "10.00");
        assertThrows(RegraDeNegocioException.class, () -> comRecebimento.cancelar("Motivo"));
    }

    @Test
    void contaComRecebimentosSoPermiteEditarDadosBasicos() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));
        receber(conta, 0, "10.00");

        assertThrows(RegraDeNegocioException.class, () -> conta.editar("Nova", null,
                new BigDecimal("200.00"), LocalDate.now().plusDays(5), null, null));

        UUID outroCliente = UUID.randomUUID();
        conta.editarDadosBasicos("Energia - outubro", outroCliente, "ajustada");
        assertThat(conta.getDescricao()).isEqualTo("Energia - outubro");
        assertThat(conta.getClienteId()).isEqualTo(outroCliente);
        assertThat(conta.getValorTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    void contaSemRecebimentosPodeTerValorEParcelasEditados() {
        ContaReceber conta = contaUnica("100.00", LocalDate.now().plusDays(10));

        conta.editar("Parcelada", null, new BigDecimal("200.00"), null,
                List.of(new DadosParcela(new BigDecimal("120.00"), LocalDate.now().plusDays(10)),
                        new DadosParcela(new BigDecimal("80.00"), LocalDate.now().plusDays(40))), null);

        assertThat(conta.getParcelas()).hasSize(2);
        assertThat(conta.getValorTotal()).isEqualByComparingTo("200.00");
        assertThrows(RegraDeNegocioException.class, () -> conta.editar("Errada", null,
                new BigDecimal("200.00"), null,
                List.of(new DadosParcela(new BigDecimal("50.00"), LocalDate.now().plusDays(10))), null));
    }

    @Test
    void calculaVencidaPeloVencimentoESaldoPendente() {
        LocalDate hoje = LocalDate.now();
        ContaReceber atrasada = contaUnica("100.00", hoje.minusDays(3));
        ContaReceber noPrazo = contaUnica("100.00", hoje.plusDays(3));
        ContaReceber venceHoje = contaUnica("100.00", hoje);

        assertThat(atrasada.estaVencida(hoje)).isTrue();
        assertThat(noPrazo.estaVencida(hoje)).isFalse();
        assertThat(venceHoje.estaVencida(hoje)).isFalse();

        receber(atrasada, 0, "100.00");
        assertThat(atrasada.estaVencida(hoje)).isFalse();
    }

    @Test
    void contaParcialmentePagaAtrasadaContinuaVencida() {
        LocalDate hoje = LocalDate.now();
        ContaReceber conta = contaUnica("100.00", hoje.minusDays(2));

        receber(conta, 0, "40.00");

        assertThat(conta.estaVencida(hoje)).isTrue();
        assertThat(conta.estaEmAberto()).isTrue();
    }
}
