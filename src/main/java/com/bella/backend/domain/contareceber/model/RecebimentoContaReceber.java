package com.bella.backend.domain.contareceber.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Registro imutável de um recebimento: nunca é alterado ou removido, para preservar o histórico.
 * <p>
 * valorRecebido abate o saldo da parcela; o desconto também abate (é parte da dívida perdoada);
 * juros e multa são acréscimos cobrados além da dívida e não alteram o saldo.
 * Entrada real em caixa = valorRecebido + juros + multa (ver getValorTotalRecebido).
 */
public class RecebimentoContaReceber {

    private final UUID id;
    private final BigDecimal valorRecebido;
    private final LocalDate dataRecebimento;
    private final BigDecimal juros;
    private final BigDecimal multa;
    private final BigDecimal desconto;
    private final String observacao;
    private final Instant criadoEm;

    private RecebimentoContaReceber(UUID id, BigDecimal valorRecebido, LocalDate dataRecebimento, BigDecimal juros,
                                 BigDecimal multa, BigDecimal desconto, String observacao, Instant criadoEm) {
        this.id = id;
        this.valorRecebido = validarValorRecebido(valorRecebido);
        this.dataRecebimento = validarData(dataRecebimento);
        this.juros = validarNaoNegativo(juros, "Juros não podem ser negativos");
        this.multa = validarNaoNegativo(multa, "Multa não pode ser negativa");
        this.desconto = validarNaoNegativo(desconto, "Desconto não pode ser negativo");
        this.observacao = observacao;
        this.criadoEm = criadoEm;
    }

    public static RecebimentoContaReceber novo(BigDecimal valorRecebido, LocalDate dataRecebimento, BigDecimal juros,
                                            BigDecimal multa, BigDecimal desconto, String observacao) {
        return new RecebimentoContaReceber(UUID.randomUUID(), valorRecebido, dataRecebimento, juros, multa, desconto,
                observacao, Instant.now());
    }

    public static RecebimentoContaReceber existente(UUID id, BigDecimal valorRecebido, LocalDate dataRecebimento,
                                                 BigDecimal juros, BigDecimal multa, BigDecimal desconto,
                                                 String observacao, Instant criadoEm) {
        return new RecebimentoContaReceber(id, valorRecebido, dataRecebimento, juros, multa, desconto, observacao, criadoEm);
    }

    public BigDecimal getValorAbatido() {
        return valorRecebido.add(desconto);
    }

    public BigDecimal getValorTotalRecebido() {
        return valorRecebido.add(juros).add(multa);
    }

    private static BigDecimal validarValorRecebido(BigDecimal valorRecebido) {
        if (valorRecebido == null || valorRecebido.signum() <= 0) {
            throw new RegraDeNegocioException("Valor recebido deve ser maior que zero");
        }
        return valorRecebido;
    }

    private static LocalDate validarData(LocalDate data) {
        if (data == null) {
            throw new RegraDeNegocioException("Data do recebimento é obrigatória");
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

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
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
