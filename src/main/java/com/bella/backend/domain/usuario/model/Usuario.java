package com.bella.backend.domain.usuario.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public class Usuario {

    public static final int TAMANHO_MINIMO_SENHA = 8;
    public static final int TAMANHO_MAXIMO_SENHA = 72; // limite do BCrypt
    private static final Pattern LOGIN_VALIDO = Pattern.compile("^[a-z0-9._-]{3,50}$");

    private final UUID id;
    private String nome;
    private final String login;
    private String senhaHash;
    private PerfilUsuario perfil;
    private StatusUsuario status;
    private Instant senhaAlteradaEm;
    private Instant ultimoAcessoEm;
    private boolean senhaProvisoria;
    private Instant redefinicaoSolicitadaEm;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    private Usuario(UUID id, String nome, String login, String senhaHash, PerfilUsuario perfil, StatusUsuario status,
                    Instant senhaAlteradaEm, Instant ultimoAcessoEm, boolean senhaProvisoria,
                    Instant redefinicaoSolicitadaEm, Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.nome = validarNome(nome);
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = exigirPerfil(perfil);
        this.status = status;
        this.senhaAlteradaEm = senhaAlteradaEm;
        this.ultimoAcessoEm = ultimoAcessoEm;
        this.senhaProvisoria = senhaProvisoria;
        this.redefinicaoSolicitadaEm = redefinicaoSolicitadaEm;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Usuario novo(String nome, String login, String senhaHash, PerfilUsuario perfil) {
        Instant agora = Instant.now();
        return new Usuario(UUID.randomUUID(), nome, normalizarLogin(login), senhaHash, perfil, StatusUsuario.ATIVO,
                agora, null, false, null, agora, agora);
    }

    public static Usuario existente(UUID id, String nome, String login, String senhaHash, PerfilUsuario perfil,
                                    StatusUsuario status, Instant senhaAlteradaEm, Instant ultimoAcessoEm,
                                    boolean senhaProvisoria, Instant redefinicaoSolicitadaEm, Instant criadoEm,
                                    Instant atualizadoEm) {
        return new Usuario(id, nome, login, senhaHash, perfil, status, senhaAlteradaEm, ultimoAcessoEm, senhaProvisoria,
                redefinicaoSolicitadaEm, criadoEm, atualizadoEm);
    }

    /** O login é gravado em minúsculas, sem espaços nas pontas, e só aceita letras, números, ponto, hífen e sublinhado. */
    public static String normalizarLogin(String login) {
        String limpo = login == null ? "" : login.trim().toLowerCase();
        if (!LOGIN_VALIDO.matcher(limpo).matches()) {
            throw new RegraDeNegocioException(
                    "Login deve ter de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado");
        }
        return limpo;
    }

    public static void validarSenhaNova(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraDeNegocioException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        if (senha.length() > TAMANHO_MAXIMO_SENHA) {
            throw new RegraDeNegocioException("A senha deve ter no máximo " + TAMANHO_MAXIMO_SENHA + " caracteres");
        }
    }

    public void editar(String nome, PerfilUsuario perfil) {
        this.nome = validarNome(nome);
        this.perfil = exigirPerfil(perfil);
        this.atualizadoEm = Instant.now();
    }

    public void ativar() {
        this.status = StatusUsuario.ATIVO;
        this.atualizadoEm = Instant.now();
    }

    public void inativar() {
        this.status = StatusUsuario.INATIVO;
        this.atualizadoEm = Instant.now();
    }

    /** Senha escolhida pela própria pessoa: encerra qualquer pedido de redefinição e a obrigação de trocar. */
    public void trocarSenha(String novoHash) {
        aplicarSenha(novoHash);
        this.senhaProvisoria = false;
    }

    /** Senha gerada pelo administrador: vale só para entrar e trocar; a pessoa é obrigada a definir uma nova. */
    public void definirSenhaProvisoria(String novoHash) {
        aplicarSenha(novoHash);
        this.senhaProvisoria = true;
    }

    private void aplicarSenha(String novoHash) {
        Instant agora = Instant.now();
        this.senhaHash = novoHash;
        this.senhaAlteradaEm = agora;
        this.redefinicaoSolicitadaEm = null;
        this.atualizadoEm = agora;
    }

    /** "Esqueci a senha": só marca o pedido (o mais antigo é mantido) para o administrador atender. */
    public void solicitarRedefinicao() {
        if (estaAtivo() && redefinicaoSolicitadaEm == null) {
            this.redefinicaoSolicitadaEm = Instant.now();
        }
    }

    public void registrarAcesso() {
        this.ultimoAcessoEm = Instant.now();
    }

    public boolean estaAtivo() {
        return status == StatusUsuario.ATIVO;
    }

    public boolean ehAdministradorAtivo() {
        return perfil == PerfilUsuario.ADMINISTRADOR && estaAtivo();
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Nome do usuário é obrigatório");
        }
        return nome.trim();
    }

    private static PerfilUsuario exigirPerfil(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new RegraDeNegocioException("Perfil do usuário é obrigatório");
        }
        return perfil;
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
