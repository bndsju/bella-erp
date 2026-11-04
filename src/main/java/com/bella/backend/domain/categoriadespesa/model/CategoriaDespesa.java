package com.bella.backend.domain.categoriadespesa.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;

public class CategoriaDespesa {

    private final UUID id;
    private String nome;
    private StatusCategoriaDespesa status;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private CategoriaDespesa(UUID id, String nome, StatusCategoriaDespesa status, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static CategoriaDespesa novo(String nome) {
        Instant agora = Instant.now();
        return new CategoriaDespesa(UUID.randomUUID(), nome, StatusCategoriaDespesa.ATIVO, agora, agora);
    }

    public static CategoriaDespesa existente(UUID id, String nome, StatusCategoriaDespesa status, Instant criadoEm,
                                       Instant atualizadoEm) {
        return new CategoriaDespesa(id, nome, status, criadoEm, atualizadoEm);
    }

    /** Categorias criadas pelo sistema são usadas por contas automáticas e não podem ser alteradas. */
    public boolean ehDoSistema() {
        return CategoriasDespesaDoSistema.COMPRA_DE_MERCADORIAS.equals(id);
    }

    public void editar(String nome) {
        exigirNaoSerDoSistema("renomeada");
        this.nome = validarNome(nome);
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusCategoriaDespesa.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        exigirNaoSerDoSistema("inativada");
        this.status = StatusCategoriaDespesa.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    private void exigirNaoSerDoSistema(String acao) {
        if (ehDoSistema()) {
            throw new RegraDeNegocioException("Categoria do sistema não pode ser " + acao
                    + ": ela é usada pelas contas a pagar geradas automaticamente pelas compras");
        }
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome da categoria de despesa é obrigatório");
        }
        return nome;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusCategoriaDespesa getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
