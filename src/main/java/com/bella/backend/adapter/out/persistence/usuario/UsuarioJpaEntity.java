package com.bella.backend.adapter.out.persistence.usuario;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios", schema = "bella")
public class UsuarioJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusUsuario status;

    @Column(name = "senha_alterada_em", nullable = false)
    private Instant senhaAlteradaEm;

    @Column(name = "ultimo_acesso_em")
    private Instant ultimoAcessoEm;

    @Column(name = "senha_provisoria", nullable = false)
    private boolean senhaProvisoria;

    @Column(name = "redefinicao_solicitada_em")
    private Instant redefinicaoSolicitadaEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(UUID id, String nome, String login, String senhaHash, PerfilUsuario perfil,
                            StatusUsuario status, Instant senhaAlteradaEm, Instant ultimoAcessoEm, boolean senhaProvisoria,
                            Instant redefinicaoSolicitadaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.status = status;
        this.senhaAlteradaEm = senhaAlteradaEm;
        this.ultimoAcessoEm = ultimoAcessoEm;
        this.senhaProvisoria = senhaProvisoria;
        this.redefinicaoSolicitadaEm = redefinicaoSolicitadaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public Instant getSenhaAlteradaEm() {
        return senhaAlteradaEm;
    }

    public Instant getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    public boolean isSenhaProvisoria() {
        return senhaProvisoria;
    }

    public Instant getRedefinicaoSolicitadaEm() {
        return redefinicaoSolicitadaEm;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
