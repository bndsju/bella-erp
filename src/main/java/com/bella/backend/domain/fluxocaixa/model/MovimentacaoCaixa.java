package com.bella.backend.domain.fluxocaixa.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Movimentação de caixa. Movimentações automáticas (de outros módulos) não podem ser editadas ou
 * canceladas manualmente; as manuais só podem ser editadas enquanto previstas. Uma movimentação
 * realizada nunca é apagada: pode, no máximo, ser cancelada (estorno), permanecendo rastreável.
 */
public class MovimentacaoCaixa {

    private final UUID id;
    private TipoMovimentacaoCaixa tipo;
    private String descricao;
    private UUID categoriaId;
    private BigDecimal valor;
    private SituacaoMovimentacaoCaixa situacao;
    private LocalDate dataPrevista;
    private LocalDate dataEfetiva;
    private final OrigemMovimentacaoCaixa origem;
    private final UUID origemReferenciaId;
    private String observacoes;
    private boolean cancelada;
    private String motivoCancelamento;
    private Instant canceladaEm;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private MovimentacaoCaixa(UUID id, TipoMovimentacaoCaixa tipo, String descricao, UUID categoriaId,
                               BigDecimal valor, SituacaoMovimentacaoCaixa situacao, LocalDate dataPrevista,
                               LocalDate dataEfetiva, OrigemMovimentacaoCaixa origem, UUID origemReferenciaId,
                               String observacoes, boolean cancelada, String motivoCancelamento,
                               Instant canceladaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.origem = validarOrigem(origem, origemReferenciaId);
        this.origemReferenciaId = origemReferenciaId;
        this.cancelada = cancelada;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladaEm = canceladaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
        aplicarDados(tipo, descricao, categoriaId, valor, situacao, dataPrevista, dataEfetiva, observacoes);
    }

    public static MovimentacaoCaixa novaManual(TipoMovimentacaoCaixa tipo, String descricao, UUID categoriaId,
                                                BigDecimal valor, SituacaoMovimentacaoCaixa situacao,
                                                LocalDate dataPrevista, LocalDate dataEfetiva,
                                                String observacoes) {
        if (categoriaId == null) {
            throw new RegraDeNegocioException("Categoria é obrigatória em lançamentos manuais");
        }
        Instant agora = Instant.now();
        return new MovimentacaoCaixa(UUID.randomUUID(), tipo, descricao, categoriaId, valor, situacao,
                dataPrevista, dataEfetiva, OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL, null, observacoes, false,
                null, null, agora, agora);
    }

    public static MovimentacaoCaixa novaAutomatica(TipoMovimentacaoCaixa tipo, String descricao, UUID categoriaId,
                                                    BigDecimal valor, SituacaoMovimentacaoCaixa situacao,
                                                    LocalDate dataPrevista, LocalDate dataEfetiva,
                                                    OrigemMovimentacaoCaixa origem, UUID origemReferenciaId,
                                                    String observacoes) {
        if (origem == OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL) {
            throw new RegraDeNegocioException("Movimentação automática não pode ter origem de lançamento manual");
        }
        Instant agora = Instant.now();
        return new MovimentacaoCaixa(UUID.randomUUID(), tipo, descricao, categoriaId, valor, situacao,
                dataPrevista, dataEfetiva, origem, origemReferenciaId, observacoes, false, null, null, agora,
                agora);
    }

    public static MovimentacaoCaixa existente(UUID id, TipoMovimentacaoCaixa tipo, String descricao,
                                               UUID categoriaId, BigDecimal valor,
                                               SituacaoMovimentacaoCaixa situacao, LocalDate dataPrevista,
                                               LocalDate dataEfetiva, OrigemMovimentacaoCaixa origem,
                                               UUID origemReferenciaId, String observacoes, boolean cancelada,
                                               String motivoCancelamento, Instant canceladaEm, Instant criadoEm,
                                               Instant atualizadoEm) {
        return new MovimentacaoCaixa(id, tipo, descricao, categoriaId, valor, situacao, dataPrevista, dataEfetiva,
                origem, origemReferenciaId, observacoes, cancelada, motivoCancelamento, canceladaEm, criadoEm,
                atualizadoEm);
    }

    public void editarManual(TipoMovimentacaoCaixa tipo, String descricao, UUID categoriaId, BigDecimal valor,
                              LocalDate dataPrevista, String observacoes) {
        exigirManual("editada");
        garantirNaoCancelada();
        if (situacao == SituacaoMovimentacaoCaixa.REALIZADO) {
            throw new RegraDeNegocioException(
                    "Lançamento realizado não pode ser editado; cancele-o (estorno) e lance novamente");
        }
        if (categoriaId == null) {
            throw new RegraDeNegocioException("Categoria é obrigatória em lançamentos manuais");
        }
        aplicarDados(tipo, descricao, categoriaId, valor, SituacaoMovimentacaoCaixa.PREVISTO, dataPrevista, null,
                observacoes);
        this.atualizadoEm = Instant.now();
    }

    /** Transforma a projeção em movimentação efetiva (mesmo registro, sem duplicar o valor). */
    public void realizar(LocalDate dataEfetiva) {
        garantirNaoCancelada();
        if (situacao == SituacaoMovimentacaoCaixa.REALIZADO) {
            throw new RegraDeNegocioException("Movimentação já está realizada");
        }
        LocalDate data = dataEfetiva == null ? LocalDate.now() : dataEfetiva;
        validarDataEfetiva(data);
        this.situacao = SituacaoMovimentacaoCaixa.REALIZADO;
        this.dataEfetiva = data;
        this.atualizadoEm = Instant.now();
    }

    public void cancelarManual(String motivo) {
        exigirManual("cancelada manualmente");
        cancelar(motivo);
    }

    /** Cancelamento disparado pelo módulo de origem (ex.: conta cancelada). */
    public void cancelarPorOrigem(String motivo) {
        if (origem == OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL) {
            throw new RegraDeNegocioException("Lançamento manual não é cancelado por módulo de origem");
        }
        cancelar(motivo);
    }

    public LocalDate getDataReferencia() {
        return situacao == SituacaoMovimentacaoCaixa.REALIZADO ? dataEfetiva : dataPrevista;
    }

    private void cancelar(String motivo) {
        garantirNaoCancelada();
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo do cancelamento é obrigatório");
        }
        cancelada = true;
        motivoCancelamento = motivo;
        canceladaEm = Instant.now();
        atualizadoEm = canceladaEm;
    }

    private void exigirManual(String acao) {
        if (origem != OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL) {
            throw new RegraDeNegocioException("Movimentação automática não pode ser " + acao + " manualmente");
        }
    }

    private void garantirNaoCancelada() {
        if (cancelada) {
            throw new RegraDeNegocioException("Movimentação cancelada não pode ser alterada");
        }
    }

    private void aplicarDados(TipoMovimentacaoCaixa tipo, String descricao, UUID categoriaId, BigDecimal valor,
                               SituacaoMovimentacaoCaixa situacao, LocalDate dataPrevista, LocalDate dataEfetiva,
                               String observacoes) {
        if (tipo == null) {
            throw new RegraDeNegocioException("Tipo da movimentação é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new RegraDeNegocioException("Descrição é obrigatória");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException("Valor da movimentação deve ser maior que zero");
        }
        if (situacao == null) {
            throw new RegraDeNegocioException("Situação da movimentação é obrigatória");
        }
        if (dataPrevista == null) {
            throw new RegraDeNegocioException("Data prevista é obrigatória");
        }
        if (situacao == SituacaoMovimentacaoCaixa.REALIZADO) {
            if (dataEfetiva == null) {
                throw new RegraDeNegocioException("Movimentação realizada deve possuir data de realização");
            }
            validarDataEfetiva(dataEfetiva);
        } else if (dataEfetiva != null) {
            throw new RegraDeNegocioException("Movimentação prevista não pode possuir data de realização");
        }

        this.tipo = tipo;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.valor = valor;
        this.situacao = situacao;
        this.dataPrevista = dataPrevista;
        this.dataEfetiva = dataEfetiva;
        this.observacoes = observacoes;
    }

    private static void validarDataEfetiva(LocalDate dataEfetiva) {
        if (dataEfetiva.isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("Data de realização não pode ser futura");
        }
    }

    private static OrigemMovimentacaoCaixa validarOrigem(OrigemMovimentacaoCaixa origem, UUID referencia) {
        if (origem == null) {
            throw new RegraDeNegocioException("Origem da movimentação é obrigatória");
        }
        if (origem == OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL && referencia != null) {
            throw new RegraDeNegocioException("Lançamento manual não possui referência de origem");
        }
        if (origem != OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL && referencia == null) {
            throw new RegraDeNegocioException("Movimentação automática deve possuir referência ao registro de origem");
        }
        return origem;
    }

    public UUID getId() {
        return id;
    }

    public TipoMovimentacaoCaixa getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public SituacaoMovimentacaoCaixa getSituacao() {
        return situacao;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public LocalDate getDataEfetiva() {
        return dataEfetiva;
    }

    public OrigemMovimentacaoCaixa getOrigem() {
        return origem;
    }

    public UUID getOrigemReferenciaId() {
        return origemReferenciaId;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public boolean isCancelada() {
        return cancelada;
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
