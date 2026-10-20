package com.bella.backend.domain.contapagar.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Obrigação financeira com uma ou mais parcelas. O status da conta é sempre derivado das
 * parcelas (ou CANCELADA); "vencida" não é status, é calculada por {@link #estaVencida}.
 * Pagamentos nunca são removidos: uma conta que já recebeu pagamento só aceita edição de dados
 * descritivos e não pode ser cancelada.
 */
public class ContaPagar {

    private final UUID id;
    private String descricao;
    private UUID fornecedorId;
    private UUID categoriaDespesaId;
    private BigDecimal valorTotal;
    private final LocalDate dataLancamento;
    private String observacoes;
    private final OrigemContaPagar origem;
    private final UUID origemReferenciaId;
    private StatusContaPagar status;
    private List<ParcelaContaPagar> parcelas;
    private String motivoCancelamento;
    private Instant canceladaEm;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private ContaPagar(UUID id, String descricao, UUID fornecedorId, UUID categoriaDespesaId,
                        BigDecimal valorTotal, LocalDate dataLancamento, String observacoes,
                        OrigemContaPagar origem, UUID origemReferenciaId, StatusContaPagar status,
                        List<ParcelaContaPagar> parcelas, String motivoCancelamento, Instant canceladaEm,
                        Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.descricao = validarDescricao(descricao);
        this.fornecedorId = fornecedorId;
        this.categoriaDespesaId = validarCategoria(categoriaDespesaId);
        this.valorTotal = validarValorTotal(valorTotal);
        this.dataLancamento = dataLancamento;
        this.observacoes = observacoes;
        this.origem = validarOrigem(origem, origemReferenciaId);
        this.origemReferenciaId = origemReferenciaId;
        this.status = status;
        this.parcelas = validarSomaDasParcelas(parcelas, this.valorTotal);
        this.motivoCancelamento = motivoCancelamento;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static ContaPagar nova(String descricao, UUID fornecedorId, UUID categoriaDespesaId,
                                   BigDecimal valorTotal, LocalDate vencimento, List<DadosParcela> dadosParcelas,
                                   String observacoes, OrigemContaPagar origem, UUID origemReferenciaId) {
        Instant agora = Instant.now();
        return new ContaPagar(UUID.randomUUID(), descricao, fornecedorId, categoriaDespesaId, valorTotal,
                LocalDate.now(), observacoes, origem == null ? OrigemContaPagar.MANUAL : origem,
                origemReferenciaId, StatusContaPagar.PENDENTE,
                montarParcelas(valorTotal, vencimento, dadosParcelas), null, null, agora, agora);
    }

    public static ContaPagar existente(UUID id, String descricao, UUID fornecedorId, UUID categoriaDespesaId,
                                        BigDecimal valorTotal, LocalDate dataLancamento, String observacoes,
                                        OrigemContaPagar origem, UUID origemReferenciaId, StatusContaPagar status,
                                        List<ParcelaContaPagar> parcelas, String motivoCancelamento,
                                        Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        return new ContaPagar(id, descricao, fornecedorId, categoriaDespesaId, valorTotal, dataLancamento,
                observacoes, origem, origemReferenciaId, status, parcelas, motivoCancelamento, canceladaEm,
                criadoEm, atualizadoEm);
    }

    /** Edição completa (inclui valor e parcelas): só enquanto não houver nenhum pagamento. */
    public void editar(String descricao, UUID fornecedorId, UUID categoriaDespesaId, BigDecimal valorTotal,
                        LocalDate vencimento, List<DadosParcela> dadosParcelas, String observacoes) {
        garantirNaoCancelada();
        if (temPagamentos()) {
            throw new RegraDeNegocioException(
                    "Conta com pagamentos registrados não pode ter valor ou parcelas alterados");
        }
        String novaDescricao = validarDescricao(descricao);
        UUID novaCategoria = validarCategoria(categoriaDespesaId);
        BigDecimal novoValor = validarValorTotal(valorTotal);
        List<ParcelaContaPagar> novasParcelas = validarSomaDasParcelas(
                montarParcelas(novoValor, vencimento, dadosParcelas), novoValor);

        this.descricao = novaDescricao;
        this.fornecedorId = fornecedorId;
        this.categoriaDespesaId = novaCategoria;
        this.valorTotal = novoValor;
        this.parcelas = novasParcelas;
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    /** Dados descritivos podem ser corrigidos mesmo com pagamentos, pois não afetam valores. */
    public void editarDadosBasicos(String descricao, UUID fornecedorId, UUID categoriaDespesaId,
                                    String observacoes) {
        garantirNaoCancelada();
        this.descricao = validarDescricao(descricao);
        this.fornecedorId = fornecedorId;
        this.categoriaDespesaId = validarCategoria(categoriaDespesaId);
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    public PagamentoContaPagar registrarPagamento(UUID parcelaId, BigDecimal valorPago, LocalDate dataPagamento,
                                                   BigDecimal juros, BigDecimal multa, BigDecimal desconto,
                                                   String observacao) {
        garantirNaoCancelada();
        if (dataPagamento != null && dataPagamento.isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("Data do pagamento não pode ser futura");
        }
        ParcelaContaPagar parcela = parcelas.stream()
                .filter(p -> p.getId().equals(parcelaId))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Parcela não pertence a esta conta"));

        PagamentoContaPagar pagamento = parcela.registrarPagamento(valorPago, dataPagamento, juros, multa,
                desconto, observacao);
        recalcularStatus();
        this.atualizadoEm = Instant.now();
        return pagamento;
    }

    public void cancelar(String motivo) {
        garantirNaoCancelada();
        if (temPagamentos()) {
            throw new RegraDeNegocioException("Conta com pagamentos registrados não pode ser cancelada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do cancelamento é obrigatório");
        }
        parcelas.forEach(ParcelaContaPagar::cancelar);
        status = StatusContaPagar.CANCELADA;
        motivoCancelamento = motivo;
        canceladaEm = Instant.now();
        atualizadoEm = canceladaEm;
    }

    public boolean temPagamentos() {
        return parcelas.stream().anyMatch(ParcelaContaPagar::temPagamentos);
    }

    public BigDecimal getValorPago() {
        return parcelas.stream().map(ParcelaContaPagar::getValorPago).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSaldoPendente() {
        return parcelas.stream().map(ParcelaContaPagar::getSaldoPendente).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean estaVencida(LocalDate hoje) {
        return parcelas.stream().anyMatch(parcela -> parcela.estaVencida(hoje));
    }

    public boolean estaEmAberto() {
        return status == StatusContaPagar.PENDENTE || status == StatusContaPagar.PARCIALMENTE_PAGA;
    }

    private void recalcularStatus() {
        List<ParcelaContaPagar> ativas = parcelas.stream()
                .filter(parcela -> parcela.getStatus() != StatusParcela.CANCELADA)
                .toList();
        if (ativas.stream().allMatch(parcela -> parcela.getStatus() == StatusParcela.PAGA)) {
            status = StatusContaPagar.PAGA;
        } else if (temPagamentos()) {
            status = StatusContaPagar.PARCIALMENTE_PAGA;
        } else {
            status = StatusContaPagar.PENDENTE;
        }
    }

    private void garantirNaoCancelada() {
        if (status == StatusContaPagar.CANCELADA) {
            throw new RegraDeNegocioException("Conta cancelada não pode ser alterada");
        }
    }

    private static List<ParcelaContaPagar> montarParcelas(BigDecimal valorTotal, LocalDate vencimento,
                                                           List<DadosParcela> dadosParcelas) {
        boolean temParcelas = dadosParcelas != null && !dadosParcelas.isEmpty();
        if (temParcelas && vencimento != null) {
            throw new RegraDeNegocioException("Informe o vencimento (pagamento único) ou as parcelas, não ambos");
        }
        if (!temParcelas) {
            if (vencimento == null) {
                throw new RegraDeNegocioException("Vencimento é obrigatório");
            }
            return List.of(ParcelaContaPagar.nova(1, valorTotal, vencimento));
        }
        List<ParcelaContaPagar> resultado = new ArrayList<>();
        for (int i = 0; i < dadosParcelas.size(); i++) {
            DadosParcela dados = dadosParcelas.get(i);
            resultado.add(ParcelaContaPagar.nova(i + 1, dados.valor(), dados.vencimento()));
        }
        return resultado;
    }

    private static List<ParcelaContaPagar> validarSomaDasParcelas(List<ParcelaContaPagar> parcelas,
                                                                   BigDecimal valorTotal) {
        if (parcelas == null || parcelas.isEmpty()) {
            throw new RegraDeNegocioException("Conta precisa ter ao menos uma parcela");
        }
        BigDecimal soma = parcelas.stream().map(ParcelaContaPagar::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (soma.compareTo(valorTotal) != 0) {
            throw new RegraDeNegocioException("A soma das parcelas deve corresponder ao valor da conta");
        }
        return new ArrayList<>(parcelas);
    }

    private static String validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("Descrição é obrigatória");
        }
        return descricao;
    }

    private static UUID validarCategoria(UUID categoriaDespesaId) {
        if (categoriaDespesaId == null) {
            throw new RegraDeNegocioException("Categoria de despesa é obrigatória");
        }
        return categoriaDespesaId;
    }

    private static BigDecimal validarValorTotal(BigDecimal valorTotal) {
        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new RegraDeNegocioException("Valor da conta deve ser maior que zero");
        }
        return valorTotal;
    }

    private static OrigemContaPagar validarOrigem(OrigemContaPagar origem, UUID origemReferenciaId) {
        if (origem == null) {
            throw new RegraDeNegocioException("Origem do lançamento é obrigatória");
        }
        if (origem != OrigemContaPagar.MANUAL && origemReferenciaId == null) {
            throw new RegraDeNegocioException("Referência de origem é obrigatória para lançamentos automáticos");
        }
        return origem;
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getFornecedorId() {
        return fornecedorId;
    }

    public UUID getCategoriaDespesaId() {
        return categoriaDespesaId;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public OrigemContaPagar getOrigem() {
        return origem;
    }

    public UUID getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public StatusContaPagar getStatus() {
        return status;
    }

    public List<ParcelaContaPagar> getParcelas() {
        return List.copyOf(parcelas);
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
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
