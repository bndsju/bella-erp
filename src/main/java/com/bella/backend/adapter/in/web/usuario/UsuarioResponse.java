package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import com.bella.backend.domain.usuario.model.Usuario;

import java.time.Instant;
import java.util.UUID;

/** Nunca inclui a senha nem o hash. */
public record UsuarioResponse(
        UUID id,
        String nome,
        String login,
        PerfilUsuario perfil,
        StatusUsuario status,
        Instant ultimoAcessoEm,
        boolean senhaProvisoria,
        Instant redefinicaoSolicitadaEm,
        Instant criadoEm
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getLogin(), u.getPerfil(), u.getStatus(),
                u.getUltimoAcessoEm(), u.isSenhaProvisoria(), u.getRedefinicaoSolicitadaEm(), u.getCriadoEm());
    }
}
