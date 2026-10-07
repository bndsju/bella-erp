package com.bella.backend.domain.compra.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Totais são sempre calculados aqui, nunca recebidos de fora:
 * valorProdutos = soma de (quantidade x custo) dos itens, antes de descontos;
 * descontoTotal = descontos dos itens + desconto geral;
 * valorTotal = valorProdutos - descontoTotal + frete + outras despesas.
 */
public class Compra {

    private final UUID id;
    private UUID fornecedorId;
    private final LocalDate dataCompra;
    private LocalDate previsaoRecebimento;
    private LocalDate dataRecebimento;
    private List<CompraItem> itens;
    private BigDecimal descontoGeral;
    private BigDecimal frete;
    private BigDecimal outrasDespesas;
    private String condicaoPagamento;
    private String observacoes;
    private StatusCompra status;
    private String motivoCancelamento;
    private Instant confirmadaEm;
    private Instant canceladaEm;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Compra(UUID id, UUID fornecedorId, LocalDate dataCompra, LocalDate previsaoRecebimento,
                    LocalDate dataRecebimento, List<CompraItem> itens, BigDecimal descontoGeral, BigDecimal frete,
                    BigDecimal outrasDespesas, String condicaoPagamento, String observacoes, StatusCompra status,
                    String motivoCancelamento, Instant confirmadaEm, Instant canceladaEm, Instant criadoEm,
                    Instant atualizadoEm) {
        this.id = id;
        this.dataCompra = dataCompra;
        this.dataRecebimento = dataRecebimento;
        this.status = status;
        this.motivoCancelamento = motivoCancelamento;
        this.confirmadaEm = confirmadaEm;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        aplicarDados(fornecedorId, previsaoRecebimento, itens, descontoGeral, frete, outrasDespesas,
                condicaoPagamento, observacoes);
    }

    public static Compra nova(UUID fornecedorId, LocalDate previsaoRecebimento, List<CompraItem> itens,
                               BigDecimal descontoGeral, BigDecimal frete, BigDecimal outrasDespesas,
                               String condicaoPagamento, String observacoes) {
        Instant agora = Instant.now();
        return new Compra(UUID.randomUUID(), fornecedorId, LocalDate.now(), previsaoRecebimento, null, itens,
                descontoGeral, frete, outrasDespesas, condicaoPagamento, observacoes, StatusCompra.RASCUNHO, null,
                null, null, agora, agora);
    }

    public static Compra existente(UUID id, UUID fornecedorId, LocalDate dataCompra, LocalDate previsaoRecebimento,
                                    LocalDate dataRecebimento, List<CompraItem> itens, BigDecimal descontoGeral,
                                    BigDecimal frete, BigDecimal outrasDespesas, String condicaoPagamento,
                                    String observacoes, StatusCompra status, String motivoCancelamento,
                                    Instant confirmadaEm, Instant canceladaEm, Instant criadoEm,
                                    Instant atualizadoEm) {
        return new Compra(id, fornecedorId, dataCompra, previsaoRecebimento, dataRecebimento, itens, descontoGeral,
                frete, outrasDespesas, condicaoPagamento, observacoes, status, motivoCancelamento, confirmadaEm,
                canceladaEm, criadoEm, atualizadoEm);
    }

    public void editar(UUID fornecedorId, LocalDate previsaoRecebimento, List<CompraItem> itens,
                        BigDecimal descontoGeral, BigDecimal frete, BigDecimal outrasDespesas,
                        String condicaoPagamento, String observacoes) {
        if (status != StatusCompra.RASCUNHO) {
            throw new RegraDeNegocioException("Só é possível editar uma compra em rascunho");
        }
        aplicarDados(fornecedorId, previsaoRecebimento, itens, descontoGeral, frete, outrasDespesas,
                condicaoPagamento, observacoes);
        this.atualizadoEm = Instant.now();
    }

    public void confirmar() {
        if (status != StatusCompra.RASCUNHO) {
            throw new RegraDeNegocioException("Só é possível confirmar uma compra em rascunho");
        }
        status = StatusCompra.CONFIRMADA;
        confirmadaEm = Instant.now();
        atualizadoEm = confirmadaEm;
    }

    public void receber(LocalDate dataRecebimento) {
        if (status == StatusCompra.RECEBIDA) {
            throw new RegraDeNegocioException("Esta compra já foi recebida");
        }
        if (status != StatusCompra.CONFIRMADA) {
            throw new RegraDeNegocioException("Só é possível receber uma compra confirmada");
        }
        this.dataRecebimento = dataRecebimento == null ? LocalDate.now() : dataRecebimento;
        status = StatusCompra.RECEBIDA;
        atualizadoEm = Instant.now();
    }

    public void cancelar(String motivo) {
        if (status == StatusCompra.RECEBIDA) {
            throw new RegraDeNegocioException("Não é possível cancelar uma compra já recebida");
        }
        if (status == StatusCompra.CANCELADA) {
            throw new RegraDeNegocioException("Esta compra já está cancelada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do cancelamento é obrigatório");
        }
        status = StatusCompra.CANCELADA;
        motivoCancelamento = motivo;
        canceladaEm = Instant.now();
        atualizadoEm = canceladaEm;
    }

    public BigDecimal getValorProdutos() {
        return itens.stream().map(CompraItem::getValorBruto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDescontoItens() {
        return itens.stream().map(CompraItem::getDesconto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDescontoTotal() {
        return getDescontoItens().add(descontoGeral);
    }

    public BigDecimal getValorTotal() {
        return getValorProdutos().subtract(getDescontoTotal()).add(frete).add(outrasDespesas);
    }

    private void aplicarDados(UUID fornecedorId, LocalDate previsaoRecebimento, List<CompraItem> itens,
                               BigDecimal descontoGeral, BigDecimal frete, BigDecimal outrasDespesas,
                               String condicaoPagamento, String observacoes) {
        if (fornecedorId == null) {
            throw new RegraDeNegocioException("Fornecedor é obrigatório");
        }
        if (previsaoRecebimento != null && previsaoRecebimento.isBefore(dataCompra)) {
            throw new RegraDeNegocioException("Previsão de recebimento não pode ser anterior à data da compra");
        }
        if (condicaoPagamento == null || condicaoPagamento.isBlank()) {
            throw new RegraDeNegocioException("Condição de pagamento é obrigatória");
        }
        List<CompraItem> itensValidados = validarItens(itens);
        BigDecimal descontoGeralValidado = validarNaoNegativo(descontoGeral, "Desconto não pode ser negativo");
        BigDecimal freteValidado = validarNaoNegativo(frete, "Frete não pode ser negativo");
        BigDecimal outrasValidado = validarNaoNegativo(outrasDespesas, "Outras despesas não podem ser negativas");

        BigDecimal liquidoItens = itensValidados.stream().map(CompraItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (descontoGeralValidado.compareTo(liquidoItens) > 0) {
            throw new RegraDeNegocioException("Desconto não pode gerar valor de produtos negativo");
        }

        this.fornecedorId = fornecedorId;
        this.previsaoRecebimento = previsaoRecebimento;
        this.itens = itensValidados;
        this.descontoGeral = descontoGeralValidado;
        this.frete = freteValidado;
        this.outrasDespesas = outrasValidado;
        this.condicaoPagamento = condicaoPagamento;
        this.observacoes = observacoes;
    }

    private static List<CompraItem> validarItens(List<CompraItem> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new RegraDeNegocioException("Compra precisa ter ao menos um item");
        }
        Set<UUID> produtos = new HashSet<>();
        for (CompraItem item : itens) {
            if (!produtos.add(item.getProdutoId())) {
                throw new RegraDeNegocioException("Produto repetido na compra: cada produto deve aparecer uma só vez");
            }
        }
        return new ArrayList<>(itens);
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

    public UUID getFornecedorId() {
        return fornecedorId;
    }

    public LocalDate getDataCompra() {
        return dataCompra;
    }

    public LocalDate getPrevisaoRecebimento() {
        return previsaoRecebimento;
    }

    public LocalDate getDataRecebimento() {
        return dataRecebimento;
    }

    public List<CompraItem> getItens() {
        return List.copyOf(itens);
    }

    public BigDecimal getDescontoGeral() {
        return descontoGeral;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getOutrasDespesas() {
        return outrasDespesas;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusCompra getStatus() {
        return status;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public Instant getConfirmadaEm() {
        return confirmadaEm;
    }

    public Instant getCanceladaEm() {
        return canceladaEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
