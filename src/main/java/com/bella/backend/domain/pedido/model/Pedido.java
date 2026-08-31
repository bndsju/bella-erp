package com.bella.backend.domain.pedido.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pedido {

    private final UUID id;
    private UUID clienteId;
    private final UUID orcamentoOrigemId;
    private UUID transportadoraId;
    private List<PedidoItem> itens;
    private BigDecimal percentualDesconto;
    private BigDecimal valorFrete;
    private String condicaoPagamento;
    private StatusPedido status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Pedido(UUID id, UUID clienteId, UUID orcamentoOrigemId, UUID transportadoraId, List<PedidoItem> itens,
                    BigDecimal percentualDesconto, BigDecimal valorFrete, String condicaoPagamento,
                    StatusPedido status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.clienteId = validarClienteId(clienteId);
        this.orcamentoOrigemId = orcamentoOrigemId;
        this.transportadoraId = transportadoraId;
        this.itens = validarItens(itens);
        this.percentualDesconto = validarPercentualDesconto(percentualDesconto);
        this.valorFrete = validarValorFrete(valorFrete);
        this.condicaoPagamento = validarCondicaoPagamento(condicaoPagamento);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Pedido novo(UUID clienteId, UUID orcamentoOrigemId, UUID transportadoraId, List<PedidoItem> itens,
                               BigDecimal percentualDesconto, BigDecimal valorFrete, String condicaoPagamento) {
        Instant agora = Instant.now();
        return new Pedido(UUID.randomUUID(), clienteId, orcamentoOrigemId, transportadoraId, itens,
                percentualDesconto, valorFrete, condicaoPagamento, StatusPedido.CRIADO, agora, agora);
    }

    public static Pedido existente(UUID id, UUID clienteId, UUID orcamentoOrigemId, UUID transportadoraId,
                                    List<PedidoItem> itens, BigDecimal percentualDesconto, BigDecimal valorFrete,
                                    String condicaoPagamento, StatusPedido status, Instant criadoEm,
                                    Instant atualizadoEm) {
        return new Pedido(id, clienteId, orcamentoOrigemId, transportadoraId, itens, percentualDesconto, valorFrete,
                condicaoPagamento, status, criadoEm, atualizadoEm);
    }

    /**
     * A origem (orçamento que deu origem ao pedido, quando houver) é imutável — é um dado
     * de rastreabilidade, não de negociação. O restante pode ser ajustado enquanto o
     * pedido não chegar a um estado final (entregue ou cancelado).
     */
    public void editar(UUID clienteId, UUID transportadoraId, List<PedidoItem> itens, BigDecimal percentualDesconto,
                        BigDecimal valorFrete, String condicaoPagamento) {
        garantirNaoFinalizado();
        this.clienteId = validarClienteId(clienteId);
        this.transportadoraId = transportadoraId;
        this.itens = validarItens(itens);
        this.percentualDesconto = validarPercentualDesconto(percentualDesconto);
        this.valorFrete = validarValorFrete(valorFrete);
        this.condicaoPagamento = validarCondicaoPagamento(condicaoPagamento);
        this.atualizadoEm = Instant.now();
    }

    public void confirmar() {
        exigirStatusAtual(StatusPedido.CRIADO, "confirmar");
        status = StatusPedido.CONFIRMADO;
        atualizadoEm = Instant.now();
    }

    public void iniciarSeparacao() {
        exigirStatusAtual(StatusPedido.CONFIRMADO, "iniciar a separação de");
        status = StatusPedido.EM_SEPARACAO;
        atualizadoEm = Instant.now();
    }

    public void marcarProntoParaEntrega() {
        exigirStatusAtual(StatusPedido.EM_SEPARACAO, "marcar como pronto para entrega");
        status = StatusPedido.PRONTO_PARA_ENTREGA;
        atualizadoEm = Instant.now();
    }

    public void entregar() {
        exigirStatusAtual(StatusPedido.PRONTO_PARA_ENTREGA, "marcar como entregue");
        status = StatusPedido.ENTREGUE;
        atualizadoEm = Instant.now();
    }

    public void cancelar() {
        if (status == StatusPedido.ENTREGUE || status == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Não é possível cancelar um pedido " + descricaoStatus(status));
        }
        status = StatusPedido.CANCELADO;
        atualizadoEm = Instant.now();
    }

    public BigDecimal getSubtotal() {
        return itens.stream()
                .map(PedidoItem::getSubtotal)
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

    private void garantirNaoFinalizado() {
        if (status == StatusPedido.ENTREGUE || status == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Pedido " + descricaoStatus(status) + " não pode ser editado");
        }
    }

    private void exigirStatusAtual(StatusPedido esperado, String acao) {
        if (status != esperado) {
            throw new RegraDeNegocioException("Só é possível " + acao + " um pedido " + descricaoStatus(esperado));
        }
    }

    private static String descricaoStatus(StatusPedido status) {
        return switch (status) {
            case CRIADO -> "recém-criado";
            case CONFIRMADO -> "confirmado";
            case EM_SEPARACAO -> "em separação";
            case PRONTO_PARA_ENTREGA -> "pronto para entrega";
            case ENTREGUE -> "entregue";
            case CANCELADO -> "cancelado";
        };
    }

    private static UUID validarClienteId(UUID clienteId) {
        if (clienteId == null) {
            throw new RegraDeNegocioException("Cliente é obrigatório");
        }
        return clienteId;
    }

    private static List<PedidoItem> validarItens(List<PedidoItem> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("Pedido precisa ter ao menos um item");
        }
        return new ArrayList<>(itens);
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

    private static String validarCondicaoPagamento(String condicaoPagamento) {
        if (condicaoPagamento == null || condicaoPagamento.isBlank()) {
            throw new RegraDeNegocioException("Condição de pagamento é obrigatória");
        }
        return condicaoPagamento;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getOrcamentoOrigemId() {
        return orcamentoOrigemId;
    }

    public UUID getTransportadoraId() {
        return transportadoraId;
    }

    public List<PedidoItem> getItens() {
        return List.copyOf(itens);
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
