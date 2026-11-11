package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corpos de requisição de autenticação e de gestão de usuários. */
public final class UsuarioRequests {

    private UsuarioRequests() {
    }

    public record Login(
            @NotBlank(message = "Login é obrigatório") String login,
            @NotBlank(message = "Senha é obrigatória") String senha) {
    }

    public record AlterarMinhaSenha(
            @NotBlank(message = "Senha atual é obrigatória") String senhaAtual,
            @NotBlank(message = "Nova senha é obrigatória") String novaSenha) {
    }

    public record Cadastrar(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotBlank(message = "Login é obrigatório") String login,
            @NotBlank(message = "Senha é obrigatória") String senha,
            @NotNull(message = "Perfil é obrigatório") PerfilUsuario perfil) {
    }

    public record Editar(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotNull(message = "Perfil é obrigatório") PerfilUsuario perfil) {
    }

    public record AlterarStatus(@NotNull(message = "Status é obrigatório") StatusUsuario status) {
    }

    public record EsqueciSenha(@NotBlank(message = "Login é obrigatório") String login) {
    }
}
