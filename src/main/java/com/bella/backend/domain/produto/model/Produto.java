package com.bella.backend.domain.produto.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Produto {

    private final UUID id;
    private final String codigoInterno;
    private String codigoBarras;
    private String nome;
    private String descricao;
    private UUID categoriaId;
    private UUID unidadeMedidaId;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private BigDecimal estoqueMinimo;
    private StatusProduto status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Produto(UUID id, String codigoInterno, String codigoBarras, String nome, String descricao,
                     UUID categoriaId, UUID unidadeMedidaId, BigDecimal precoCusto, BigDecimal precoVenda,
                     BigDecimal estoqueMinimo, StatusProduto status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.codigoInterno = validarCodigoInterno(codigoInterno);
        this.codigoBarras = codigoBarras;
        this.nome = validarNome(nome);
        this.descricao = descricao;
        this.categoriaId = validarNaoNulo(categoriaId, "Categoria é obrigatória");
        this.unidadeMedidaId = validarNaoNulo(unidadeMedidaId, "Unidade de medida é obrigatória");
        this.precoCusto = validarNaoNegativo(precoCusto, "Preço de custo não pode ser negativo");
        this.precoVenda = validarNaoNegativo(precoVenda, "Preço de venda não pode ser negativo");
        this.estoqueMinimo = validarNaoNegativo(estoqueMinimo, "Estoque mínimo não pode ser negativo");
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Produto novo(String codigoInterno, String codigoBarras, String nome, String descricao,
                                UUID categoriaId, UUID unidadeMedidaId, BigDecimal precoCusto, BigDecimal precoVenda,
                                BigDecimal estoqueMinimo) {
        Instant agora = Instant.now();
        return new Produto(UUID.randomUUID(), codigoInterno, codigoBarras, nome, descricao, categoriaId,
                unidadeMedidaId, precoCusto, precoVenda, estoqueMinimo, StatusProduto.ATIVO, agora, agora);
    }

    public static Produto existente(UUID id, String codigoInterno, String codigoBarras, String nome,
                                     String descricao, UUID categoriaId, UUID unidadeMedidaId,
                                     BigDecimal precoCusto, BigDecimal precoVenda, BigDecimal estoqueMinimo,
                                     StatusProduto status, Instant criadoEm, Instant atualizadoEm) {
        return new Produto(id, codigoInterno, codigoBarras, nome, descricao, categoriaId, unidadeMedidaId,
                precoCusto, precoVenda, estoqueMinimo, status, criadoEm, atualizadoEm);
    }

    /**
     * O código interno é a identidade estável do produto e não é editável, assim como CPF/CNPJ em Cliente.
     */
    public void editar(String codigoBarras, String nome, String descricao, UUID categoriaId, UUID unidadeMedidaId,
                        BigDecimal precoCusto, BigDecimal precoVenda, BigDecimal estoqueMinimo) {
        this.codigoBarras = codigoBarras;
        this.nome = validarNome(nome);
        this.descricao = descricao;
        this.categoriaId = validarNaoNulo(categoriaId, "Categoria é obrigatória");
        this.unidadeMedidaId = validarNaoNulo(unidadeMedidaId, "Unidade de medida é obrigatória");
        this.precoCusto = validarNaoNegativo(precoCusto, "Preço de custo não pode ser negativo");
        this.precoVenda = validarNaoNegativo(precoVenda, "Preço de venda não pode ser negativo");
        this.estoqueMinimo = validarNaoNegativo(estoqueMinimo, "Estoque mínimo não pode ser negativo");
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusProduto.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusProduto.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private static String validarCodigoInterno(String codigoInterno) {
        if (codigoInterno == null || codigoInterno.isBlank()) {
            throw new RegraDeNegocioException("Código interno é obrigatório");
        }
        return codigoInterno;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do produto é obrigatório");
        }
        return nome;
    }

    private static UUID validarNaoNulo(UUID valor, String mensagem) {
        if (valor == null) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor;
    }

    private static BigDecimal validarNaoNegativo(BigDecimal valor, String mensagem) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraDeNegocioException(mensagem);
        }
        return valor;
    }

    public UUID getId() {
        return id;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public UUID getUnidadeMedidaId() {
        return unidadeMedidaId;
    }

    public BigDecimal getPrecoCusto() {
        return precoCusto;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
