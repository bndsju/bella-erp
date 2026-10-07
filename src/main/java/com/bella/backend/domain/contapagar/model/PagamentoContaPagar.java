package com.bella.backend.domain.contapagar.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Registro imutável de um pagamento: nunca é alterado ou removido, para preservar o histórico.
 * <p>
 * valorPago abate o saldo da parcela; o desconto também abate (é parte da dívida perdoada);
 * juros e multa são acréscimos pagos além da dívida e não alteram o saldo.
 * Desembolso real de caixa = valorPago + juros + multa.
 */
public class PagamentoContaPagar {

    private final UUID id;
    private final BigDecimal valorPago;
    private final LocalDate dataPagamento;
    private final BigDecimal juros;
    private final BigDecimal multa;
    private final BigDecimal desconto;
    private final String observacao;
    private final Instant criadoEm;

    private PagamentoContaPagar(UUID id, BigDecimal valorPago, LocalDate dataPagamento, BigDecimal juros,
                                 BigDecimal multa, BigDecimal desconto, String observacao, Instant criadoEm) {
        this.id = id;
        this.valorPago = validarValorPago(valorPago);
        this.dataPagamento = validarData(dataPagamento);
        this.juros = validarNaoNegativo(juros, "Juros não podem ser negativos");
        this.multa = validarNaoNegativo(multa, "Multa não pode ser negativa");
        this.desconto = validarNaoNegativo(desconto, "Desconto não pode ser negativo");
        this.observacao = observacao;
        this.criadoEm = criadoEm;
    }

    public static PagamentoContaPagar novo(BigDecimal valorPago, LocalDate dataPagamento, BigDecimal juros,
                                            BigDecimal multa, BigDecimal desconto, String observacao) {
        return new PagamentoContaPagar(UUID.randomUUID(), valorPago, dataPagamento, juros, multa, desconto,
                observacao, Instant.now());
    }

    public static PagamentoContaPagar existente(UUID id, BigDecimal valorPago, LocalDate dataPagamento,
                                                 BigDecimal juros, BigDecimal multa, BigDecimal desconto,
                                                 String observacao, Instant criadoEm) {
        return new PagamentoContaPagar(id, valorPago, dataPagamento, juros, multa, desconto, observacao, criadoEm);
    }

    public BigDecimal getValorAbatido() {
        return valorPago.add(desconto);
    }

    public BigDecimal getValorDesembolsado() {
        return valorPago.add(juros).add(multa);
    }

    private static BigDecimal validarValorPago(BigDecimal valorPago) {
        if (valorPago == null || valorPago.signum() <= 0) {
            throw new RegraDeNegocioException("Valor pago deve ser maior que zero");
        }
        return valorPago;
    }

    private static LocalDate validarData(LocalDate data) {
        if (data == null) {
            throw new RegraDeNegocioException("Data do pagamento é obrigatória");
        }
        return data;
    }

    private static BigDecimal validarNaoNegativo(BigDecimal valor, String mensagem) {
        BigDecimal resultado = valor == null ? BigDecimal.ZERO : valor;
        if (resultado.signum() < 0) {
            throw new RegraDeNegocioException(mensagem);
        }
        return resultado;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public BigDecimal getJuros() {
        return juros;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public String getObservacao() {
        return observacao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
