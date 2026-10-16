package com.bella.backend.domain.contareceber.model;

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
 * Recebimentos nunca são removidos: uma conta que já recebeu algum valor só aceita edição de dados
 * descritivos e não pode ser cancelada.
 */
public class ContaReceber {

    private final UUID id;
    private String descricao;
    private UUID clienteId;
    private BigDecimal valorTotal;
    private final LocalDate dataLancamento;
    private String observacoes;
    private final OrigemContaReceber origem;
    private final UUID origemReferenciaId;
    private StatusContaReceber status;
    private List<ParcelaContaReceber> parcelas;
    private String motivoCancelamento;
    private Instant canceladaEm;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private ContaReceber(UUID id, String descricao, UUID clienteId,
                        BigDecimal valorTotal, LocalDate dataLancamento, String observacoes,
                        OrigemContaReceber origem, UUID origemReferenciaId, StatusContaReceber status,
                        List<ParcelaContaReceber> parcelas, String motivoCancelamento, Instant canceladaEm,
                        Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.descricao = validarDescricao(descricao);
        this.clienteId = clienteId;
        this.valorTotal = validarValorTotal(valorTotal);
        this.dataLancamento = dataLancamento;
        this.observacoes = observacoes;
        this.origem = validarOrigem(origem, clienteId, origemReferenciaId);
        this.origemReferenciaId = origemReferenciaId;
        this.status = status;
        this.parcelas = validarSomaDasParcelas(parcelas, this.valorTotal);
        this.motivoCancelamento = motivoCancelamento;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static ContaReceber nova(String descricao, UUID clienteId,
                                   BigDecimal valorTotal, LocalDate vencimento, List<DadosParcela> dadosParcelas,
                                   String observacoes, OrigemContaReceber origem, UUID origemReferenciaId) {
        Instant agora = Instant.now();
        return new ContaReceber(UUID.randomUUID(), descricao, clienteId, valorTotal,
                LocalDate.now(), observacoes, origem == null ? OrigemContaReceber.MANUAL : origem,
                origemReferenciaId, StatusContaReceber.PENDENTE,
                montarParcelas(valorTotal, vencimento, dadosParcelas), null, null, agora, agora);
    }

    public static ContaReceber existente(UUID id, String descricao, UUID clienteId,
                                        BigDecimal valorTotal, LocalDate dataLancamento, String observacoes,
                                        OrigemContaReceber origem, UUID origemReferenciaId, StatusContaReceber status,
                                        List<ParcelaContaReceber> parcelas, String motivoCancelamento,
                                        Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        return new ContaReceber(id, descricao, clienteId, valorTotal, dataLancamento,
                observacoes, origem, origemReferenciaId, status, parcelas, motivoCancelamento, canceladaEm,
                criadoEm, atualizadoEm);
    }

    /** Edição completa (inclui valor e parcelas): só enquanto não houver nenhum recebimento. */
    public void editar(String descricao, UUID clienteId, BigDecimal valorTotal,
                        LocalDate vencimento, List<DadosParcela> dadosParcelas, String observacoes) {
        garantirNaoCancelada();
        if (temRecebimentos()) {
            throw new RegraDeNegocioException(
                    "Conta com recebimentos registrados não pode ter valor ou parcelas alterados");
        }
        String novaDescricao = validarDescricao(descricao);
        validarOrigem(origem, clienteId, origemReferenciaId);
        BigDecimal novoValor = validarValorTotal(valorTotal);
        List<ParcelaContaReceber> novasParcelas = validarSomaDasParcelas(
                montarParcelas(novoValor, vencimento, dadosParcelas), novoValor);

        this.descricao = novaDescricao;
        this.clienteId = clienteId;
        this.valorTotal = novoValor;
        this.parcelas = novasParcelas;
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    /** Dados descritivos podem ser corrigidos mesmo com recebimentos, pois não afetam valores. */
    public void editarDadosBasicos(String descricao, UUID clienteId,
                                    String observacoes) {
        garantirNaoCancelada();
        validarOrigem(origem, clienteId, origemReferenciaId);
        this.descricao = validarDescricao(descricao);
        this.clienteId = clienteId;
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    public RecebimentoContaReceber registrarRecebimento(UUID parcelaId, BigDecimal valorRecebido, LocalDate dataRecebimento,
                                                   BigDecimal juros, BigDecimal multa, BigDecimal desconto,
                                                   String observacao) {
        garantirNaoCancelada();
        if (dataRecebimento != null && dataRecebimento.isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("Data do recebimento não pode ser futura");
        }
        ParcelaContaReceber parcela = parcelas.stream()
                .filter(p -> p.getId().equals(parcelaId))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Parcela não pertence a esta conta"));

        RecebimentoContaReceber recebimento = parcela.registrarRecebimento(valorRecebido, dataRecebimento, juros, multa,
                desconto, observacao);
        recalcularStatus();
        this.atualizadoEm = Instant.now();
        return recebimento;
    }

    public void cancelar(String motivo) {
        garantirNaoCancelada();
        if (temRecebimentos()) {
            throw new RegraDeNegocioException("Conta com recebimentos registrados não pode ser cancelada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do cancelamento é obrigatório");
        }
        parcelas.forEach(ParcelaContaReceber::cancelar);
        status = StatusContaReceber.CANCELADA;
        motivoCancelamento = motivo;
        canceladaEm = Instant.now();
        atualizadoEm = canceladaEm;
    }

    public boolean temRecebimentos() {
        return parcelas.stream().anyMatch(ParcelaContaReceber::temRecebimentos);
    }

    public BigDecimal getValorRecebido() {
        return parcelas.stream().map(ParcelaContaReceber::getValorRecebido).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSaldoPendente() {
        return parcelas.stream().map(ParcelaContaReceber::getSaldoPendente).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean estaVencida(LocalDate hoje) {
        return parcelas.stream().anyMatch(parcela -> parcela.estaVencida(hoje));
    }

    public boolean estaEmAberto() {
        return status == StatusContaReceber.PENDENTE || status == StatusContaReceber.PARCIALMENTE_RECEBIDA;
    }

    private void recalcularStatus() {
        List<ParcelaContaReceber> ativas = parcelas.stream()
                .filter(parcela -> parcela.getStatus() != StatusParcela.CANCELADA)
                .toList();
        if (ativas.stream().allMatch(parcela -> parcela.getStatus() == StatusParcela.RECEBIDA)) {
            status = StatusContaReceber.RECEBIDA;
        } else if (temRecebimentos()) {
            status = StatusContaReceber.PARCIALMENTE_RECEBIDA;
        } else {
            status = StatusContaReceber.PENDENTE;
        }
    }

    private void garantirNaoCancelada() {
        if (status == StatusContaReceber.CANCELADA) {
            throw new RegraDeNegocioException("Conta cancelada não pode ser alterada");
        }
    }

    private static List<ParcelaContaReceber> montarParcelas(BigDecimal valorTotal, LocalDate vencimento,
                                                           List<DadosParcela> dadosParcelas) {
        boolean temParcelas = dadosParcelas != null && !dadosParcelas.isEmpty();
        if (temParcelas && vencimento != null) {
            throw new RegraDeNegocioException("Informe o vencimento (recebimento único) ou as parcelas, não ambos");
        }
        if (!temParcelas) {
            if (vencimento == null) {
                throw new RegraDeNegocioException("Vencimento é obrigatório");
            }
            return List.of(ParcelaContaReceber.nova(1, valorTotal, vencimento));
        }
        List<ParcelaContaReceber> resultado = new ArrayList<>();
        for (int i = 0; i < dadosParcelas.size(); i++) {
            DadosParcela dados = dadosParcelas.get(i);
            resultado.add(ParcelaContaReceber.nova(i + 1, dados.valor(), dados.vencimento()));
        }
        return resultado;
    }

    private static List<ParcelaContaReceber> validarSomaDasParcelas(List<ParcelaContaReceber> parcelas,
                                                                   BigDecimal valorTotal) {
        if (parcelas == null || parcelas.isEmpty()) {
            throw new RegraDeNegocioException("Conta precisa ter ao menos uma parcela");
        }
        BigDecimal soma = parcelas.stream().map(ParcelaContaReceber::getValor)
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


    private static BigDecimal validarValorTotal(BigDecimal valorTotal) {
        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new RegraDeNegocioException("Valor da conta deve ser maior que zero");
        }
        return valorTotal;
    }

    private static OrigemContaReceber validarOrigem(OrigemContaReceber origem, UUID clienteId,
                                                      UUID origemReferenciaId) {
        if (origem == null) {
            throw new RegraDeNegocioException("Origem do lançamento é obrigatória");
        }
        if (origem != OrigemContaReceber.MANUAL && origemReferenciaId == null) {
            throw new RegraDeNegocioException("Referência de origem é obrigatória para lançamentos automáticos");
        }
        if (origem != OrigemContaReceber.MANUAL && clienteId == null) {
            throw new RegraDeNegocioException("Cliente é obrigatório para lançamentos de origem comercial");
        }
        return origem;
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getClienteId() {
        return clienteId;
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

    public OrigemContaReceber getOrigem() {
        return origem;
    }

    public UUID getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public StatusContaReceber getStatus() {
        return status;
    }

    public List<ParcelaContaReceber> getParcelas() {
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
