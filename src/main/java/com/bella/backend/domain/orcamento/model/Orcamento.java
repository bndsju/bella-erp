package com.bella.backend.domain.orcamento.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Orcamento {

    private final UUID id;
    private UUID clienteId;
    private List<OrcamentoItem> itens;
    private BigDecimal percentualDesconto;
    private BigDecimal valorFrete;
    private LocalDate prazoValidade;
    private String condicaoPagamento;
    private String observacoes;
    private StatusOrcamento status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Orcamento(UUID id, UUID clienteId, List<OrcamentoItem> itens, BigDecimal percentualDesconto,
                       BigDecimal valorFrete, LocalDate prazoValidade, String condicaoPagamento, String observacoes,
                       StatusOrcamento status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.clienteId = validarClienteId(clienteId);
        this.itens = validarItens(itens);
        this.percentualDesconto = validarPercentualDesconto(percentualDesconto);
        this.valorFrete = validarValorFrete(valorFrete);
        this.prazoValidade = validarPrazoValidadeNaoNulo(prazoValidade);
        this.condicaoPagamento = validarCondicaoPagamento(condicaoPagamento);
        this.observacoes = observacoes;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Orcamento novo(UUID clienteId, List<OrcamentoItem> itens, BigDecimal percentualDesconto,
                                  BigDecimal valorFrete, LocalDate prazoValidade, String condicaoPagamento,
                                  String observacoes) {
        validarPrazoValidadeFutura(prazoValidade);
        Instant agora = Instant.now();
        return new Orcamento(UUID.randomUUID(), clienteId, itens, percentualDesconto, valorFrete, prazoValidade,
                condicaoPagamento, observacoes, StatusOrcamento.RASCUNHO, agora, agora);
    }

    public static Orcamento existente(UUID id, UUID clienteId, List<OrcamentoItem> itens,
                                       BigDecimal percentualDesconto, BigDecimal valorFrete, LocalDate prazoValidade,
                                       String condicaoPagamento, String observacoes, StatusOrcamento status,
                                       Instant criadoEm, Instant atualizadoEm) {
        return new Orcamento(id, clienteId, itens, percentualDesconto, valorFrete, prazoValidade, condicaoPagamento,
                observacoes, status, criadoEm, atualizadoEm);
    }

    /**
     * Conteúdo (cliente, itens, desconto, frete, prazo, condição de pagamento, observações)
     * só pode ser alterado enquanto o orçamento estiver em rascunho; depois disso, a
     * negociação segue pelas transições de status (enviar/aprovar/recusar/cancelar).
     */
    public void editar(UUID clienteId, List<OrcamentoItem> itens, BigDecimal percentualDesconto,
                        BigDecimal valorFrete, LocalDate prazoValidade, String condicaoPagamento,
                        String observacoes) {
        garantirRascunho();
        validarPrazoValidadeFutura(prazoValidade);
        this.clienteId = validarClienteId(clienteId);
        this.itens = validarItens(itens);
        this.percentualDesconto = validarPercentualDesconto(percentualDesconto);
        this.valorFrete = validarValorFrete(valorFrete);
        this.prazoValidade = prazoValidade;
        this.condicaoPagamento = validarCondicaoPagamento(condicaoPagamento);
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    public void enviar() {
        if (status != StatusOrcamento.RASCUNHO) {
            throw new RegraDeNegocioException("Só é possível enviar um orçamento em rascunho");
        }
        if (itens.isEmpty()) {
            throw new RegraDeNegocioException("Orçamento precisa ter ao menos um item para ser enviado");
        }
        status = StatusOrcamento.ENVIADO;
        atualizadoEm = Instant.now();
    }

    public void aprovar() {
        if (status != StatusOrcamento.ENVIADO) {
            throw new RegraDeNegocioException("Só é possível aprovar um orçamento enviado");
        }
        status = StatusOrcamento.APROVADO;
        atualizadoEm = Instant.now();
    }

    public void recusar() {
        if (status != StatusOrcamento.ENVIADO) {
            throw new RegraDeNegocioException("Só é possível recusar um orçamento enviado");
        }
        status = StatusOrcamento.RECUSADO;
        atualizadoEm = Instant.now();
    }

    public void cancelar() {
        if (status != StatusOrcamento.RASCUNHO && status != StatusOrcamento.ENVIADO) {
            throw new RegraDeNegocioException("Só é possível cancelar um orçamento em rascunho ou enviado");
        }
        status = StatusOrcamento.CANCELADO;
        atualizadoEm = Instant.now();
    }

    /**
     * Não existe agendador nesta aplicação: a expiração é avaliada de forma preguiçosa
     * (lazy) sempre que o orçamento é lido, e persistida pelo chamador se o status mudar.
     */
    public boolean expirarSeNecessario() {
        if (status == StatusOrcamento.ENVIADO && prazoValidade.isBefore(LocalDate.now())) {
            status = StatusOrcamento.EXPIRADO;
            atualizadoEm = Instant.now();
            return true;
        }
        return false;
    }

    public BigDecimal getSubtotal() {
        return itens.stream()
                .map(OrcamentoItem::getSubtotal)
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

    private void garantirRascunho() {
        if (status != StatusOrcamento.RASCUNHO) {
            throw new RegraDeNegocioException("Orçamento só pode ser editado enquanto estiver em rascunho");
        }
    }

    private static UUID validarClienteId(UUID clienteId) {
        if (clienteId == null) {
            throw new RegraDeNegocioException("Cliente é obrigatório");
        }
        return clienteId;
    }

    private static List<OrcamentoItem> validarItens(List<OrcamentoItem> itens) {
        if (itens == null) {
            throw new RegraDeNegocioException("Lista de itens é obrigatória");
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

    private static LocalDate validarPrazoValidadeNaoNulo(LocalDate prazoValidade) {
        if (prazoValidade == null) {
            throw new RegraDeNegocioException("Prazo de validade é obrigatório");
        }
        return prazoValidade;
    }

    private static void validarPrazoValidadeFutura(LocalDate prazoValidade) {
        if (prazoValidade == null) {
            throw new RegraDeNegocioException("Prazo de validade é obrigatório");
        }
        if (prazoValidade.isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("Prazo de validade não pode ser uma data passada");
        }
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

    public List<OrcamentoItem> getItens() {
        return List.copyOf(itens);
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public LocalDate getPrazoValidade() {
        return prazoValidade;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusOrcamento getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
