package com.bella.backend.adapter.in.web.seguranca;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.Usuario;

import java.util.UUID;

/** Quem está autenticado na requisição corrente (principal do Spring Security). */
public record UsuarioAutenticado(UUID id, String login, String nome, PerfilUsuario perfil) {

    public static UsuarioAutenticado de(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getLogin(), usuario.getNome(), usuario.getPerfil());
    }
}
