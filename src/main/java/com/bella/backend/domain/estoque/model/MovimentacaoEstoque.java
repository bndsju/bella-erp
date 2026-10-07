package com.bella.backend.domain.estoque.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Registro histórico e imutável de uma alteração de estoque. Uma vez criado, um registro
 * nunca é alterado ou removido — é a fonte de auditoria de todo o módulo.
 */
public class MovimentacaoEstoque {

    private final UUID id;
    private final UUID produtoId;
    private final TipoMovimentacaoEstoque tipo;
    private final BigDecimal quantidade;
    private final String motivo;
    private final String observacao;
    private final OrigemMovimentacaoEstoque origem;
    private final UUID origemOperacaoId;
    private final String usuarioResponsavel;
    private final Instant dataHora;

    private MovimentacaoEstoque(UUID id, UUID produtoId, TipoMovimentacaoEstoque tipo, BigDecimal quantidade,
                                 String motivo, String observacao, OrigemMovimentacaoEstoque origem,
                                 UUID origemOperacaoId, String usuarioResponsavel, Instant dataHora) {
        this.id = id;
        this.produtoId = validarNaoNulo(produtoId, "Produto é obrigatório");
        this.tipo = validarNaoNulo(tipo, "Tipo de movimentação é obrigatório");
        this.quantidade = validarQuantidade(quantidade);
        this.motivo = validarMotivo(motivo);
        this.observacao = observacao;
        this.origem = validarNaoNulo(origem, "Origem da movimentação é obrigatória");
        this.origemOperacaoId = origemOperacaoId;
        this.usuarioResponsavel = usuarioResponsavel;
        this.dataHora = dataHora;
    }

    public static MovimentacaoEstoque registrar(UUID produtoId, TipoMovimentacaoEstoque tipo, BigDecimal quantidade,
                                                  String motivo, String observacao, OrigemMovimentacaoEstoque origem,
                                                  UUID origemOperacaoId, String usuarioResponsavel) {
        return new MovimentacaoEstoque(UUID.randomUUID(), produtoId, tipo, quantidade, motivo, observacao, origem,
                origemOperacaoId, usuarioResponsavel, Instant.now());
    }

    public static MovimentacaoEstoque existente(UUID id, UUID produtoId, TipoMovimentacaoEstoque tipo,
                                                  BigDecimal quantidade, String motivo, String observacao,
                                                  OrigemMovimentacaoEstoque origem, UUID origemOperacaoId,
                                                  String usuarioResponsavel, Instant dataHora) {
        return new MovimentacaoEstoque(id, produtoId, tipo, quantidade, motivo, observacao, origem, origemOperacaoId,
                usuarioResponsavel, dataHora);
    }

    private static <T> T validarNaoNulo(T valor, String mensagem) {
        if (valor == null) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor;
    }

    private static BigDecimal validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraDeNegocioException("Quantidade da movimentação deve ser maior que zero");
        }
        return quantidade;
    }

    private static String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo da movimentação é obrigatório");
        }
        return motivo;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public TipoMovimentacaoEstoque getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getObservacao() {
        return observacao;
    }

    public OrigemMovimentacaoEstoque getOrigem() {
        return origem;
    }

    public UUID getOrigemOperacaoId() {
        return origemOperacaoId;
    }

    public String getUsuarioResponsavel() {
        return usuarioResponsavel;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
