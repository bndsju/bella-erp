package com.bella.backend.domain.venda.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Uma venda é o registro definitivo de uma operação comercial concluída — nasce da
 * conclusão de um pedido e não tem caso de uso de edição: uma vez registrada, os
 * dados comerciais (cliente, itens, valores, pagamento) são imutáveis. A única
 * transição possível depois é o cancelamento.
 */
public class Venda {

    private final UUID id;
    private final UUID pedidoId;
    private final UUID clienteId;
    private final List<VendaItem> itens;
    private final BigDecimal percentualDesconto;
    private final BigDecimal valorFrete;
    private final FormaPagamento formaPagamento;
    private final String condicaoPagamento;
    private StatusVenda status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Venda(UUID id, UUID pedidoId, UUID clienteId, List<VendaItem> itens, BigDecimal percentualDesconto,
                   BigDecimal valorFrete, FormaPagamento formaPagamento, String condicaoPagamento,
                   StatusVenda status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.pedidoId = validarPedidoId(pedidoId);
        this.clienteId = validarClienteId(clienteId);
        this.itens = validarItens(itens);
        this.percentualDesconto = validarPercentualDesconto(percentualDesconto);
        this.valorFrete = validarValorFrete(valorFrete);
        this.formaPagamento = validarFormaPagamento(formaPagamento);
        this.condicaoPagamento = validarCondicaoPagamento(condicaoPagamento);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Venda concluir(UUID pedidoId, UUID clienteId, List<VendaItem> itens,
                                  BigDecimal percentualDesconto, BigDecimal valorFrete,
                                  FormaPagamento formaPagamento, String condicaoPagamento) {
        Instant agora = Instant.now();
        return new Venda(UUID.randomUUID(), pedidoId, clienteId, itens, percentualDesconto, valorFrete,
                formaPagamento, condicaoPagamento, StatusVenda.CONCLUIDA, agora, agora);
    }

    public static Venda existente(UUID id, UUID pedidoId, UUID clienteId, List<VendaItem> itens,
                                   BigDecimal percentualDesconto, BigDecimal valorFrete,
                                   FormaPagamento formaPagamento, String condicaoPagamento, StatusVenda status,
                                   Instant criadoEm, Instant atualizadoEm) {
        return new Venda(id, pedidoId, clienteId, itens, percentualDesconto, valorFrete, formaPagamento,
                condicaoPagamento, status, criadoEm, atualizadoEm);
    }

    public void cancelar() {
        if (status != StatusVenda.CONCLUIDA) {
            throw new RegraDeNegocioException("Só é possível cancelar uma venda concluída");
        }
        status = StatusVenda.CANCELADA;
        atualizadoEm = Instant.now();
    }

    public BigDecimal getSubtotal() {
        return itens.stream()
                .map(VendaItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorDesconto() {
        return getSubtotal()
                .multiply(percentualDesconto)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorTotal() {
        return getSubtotal().subtract(getValorDesconto()).add(valorFrete);
    }

    private static UUID validarPedidoId(UUID pedidoId) {
        if (pedidoId == null) {
            throw new RegraDeNegocioException("Pedido de origem é obrigatório");
        }
        return pedidoId;
    }

    private static UUID validarClienteId(UUID clienteId) {
        if (clienteId == null) {
            throw new RegraDeNegocioException("Cliente é obrigatório");
        }
        return clienteId;
    }

    private static List<VendaItem> validarItens(List<VendaItem> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("Venda precisa ter ao menos um item");
        }
        return List.copyOf(itens);
    }

    private static BigDecimal validarPercentualDesconto(BigDecimal percentualDesconto) {
        if (percentualDesconto == null
                || percentualDesconto.signum() < 0
                || percentualDesconto.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RegraDeNegocioException("Percentual de desconto deve estar entre 0 e 100");
        }
        return percentualDesconto;
    }

    private static BigDecimal validarValorFrete(BigDecimal valorFrete) {
        if (valorFrete == null || valorFrete.signum() < 0) {
            throw new RegraDeNegocioException("Valor do frete não pode ser negativo");
        }
        return valorFrete;
    }

    private static FormaPagamento validarFormaPagamento(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            throw new RegraDeNegocioException("Forma de pagamento é obrigatória");
        }
        return formaPagamento;
    }

    private static String validarCondicaoPagamento(String condicaoPagamento) {
        if (condicaoPagamento == null || condicaoPagamento.isBlank()) {
            throw new RegraDeNegocioException("Condição de pagamento é obrigatória");
        }
        return condicaoPagamento;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public List<VendaItem> getItens() {
        return itens;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
