package com.bella.backend.domain.contapagar.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParcelaContaPagar {

    private final UUID id;
    private final int numero;
    private final BigDecimal valor;
    private final LocalDate vencimento;
    private StatusParcela status;
    private final List<PagamentoContaPagar> pagamentos;

    private ParcelaContaPagar(UUID id, int numero, BigDecimal valor, LocalDate vencimento, StatusParcela status,
                               List<PagamentoContaPagar> pagamentos) {
        this.id = id;
        this.numero = numero;
        this.valor = validarValor(valor);
        this.vencimento = validarVencimento(vencimento);
        this.status = status;
        this.pagamentos = new ArrayList<>(pagamentos);
    }

    public static ParcelaContaPagar nova(int numero, BigDecimal valor, LocalDate vencimento) {
        return new ParcelaContaPagar(UUID.randomUUID(), numero, valor, vencimento, StatusParcela.PENDENTE,
                List.of());
    }

    public static ParcelaContaPagar existente(UUID id, int numero, BigDecimal valor, LocalDate vencimento,
                                               StatusParcela status, List<PagamentoContaPagar> pagamentos) {
        return new ParcelaContaPagar(id, numero, valor, vencimento, status, pagamentos);
    }

    PagamentoContaPagar registrarPagamento(BigDecimal valorPago, LocalDate dataPagamento, BigDecimal juros,
                                            BigDecimal multa, BigDecimal desconto, String observacao) {
        if (status == StatusParcela.CANCELADA) {
            throw new RegraDeNegocioException("Parcela cancelada não pode receber pagamentos");
        }
        if (status == StatusParcela.PAGA) {
            throw new RegraDeNegocioException("Parcela já está paga");
        }

        PagamentoContaPagar pagamento = PagamentoContaPagar.novo(valorPago, dataPagamento, juros, multa, desconto,
                observacao);
        if (pagamento.getValorAbatido().compareTo(getSaldoPendente()) > 0) {
            throw new RegraDeNegocioException("Pagamento não pode ultrapassar o saldo pendente da parcela");
        }

        pagamentos.add(pagamento);
        status = getSaldoPendente().signum() == 0 ? StatusParcela.PAGA : StatusParcela.PARCIALMENTE_PAGA;
        return pagamento;
    }

    void cancelar() {
        status = StatusParcela.CANCELADA;
    }

    public BigDecimal getValorPago() {
        return pagamentos.stream().map(PagamentoContaPagar::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorAbatido() {
        return pagamentos.stream().map(PagamentoContaPagar::getValorAbatido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSaldoPendente() {
        if (status == StatusParcela.CANCELADA) {
            return BigDecimal.ZERO;
        }
        return valor.subtract(getValorAbatido());
    }

    public boolean estaVencida(LocalDate hoje) {
        return (status == StatusParcela.PENDENTE || status == StatusParcela.PARCIALMENTE_PAGA)
                && vencimento.isBefore(hoje);
    }

    public boolean temPagamentos() {
        return !pagamentos.isEmpty();
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException("Valor da parcela deve ser maior que zero");
        }
        return valor;
    }

    private static LocalDate validarVencimento(LocalDate vencimento) {
        if (vencimento == null) {
            throw new RegraDeNegocioException("Vencimento da parcela é obrigatório");
        }
        return vencimento;
    }

    public UUID getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public StatusParcela getStatus() {
        return status;
    }

    public List<PagamentoContaPagar> getPagamentos() {
        return List.copyOf(pagamentos);
    }
}
