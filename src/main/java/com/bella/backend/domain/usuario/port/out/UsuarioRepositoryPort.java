package com.bella.backend.domain.usuario.port.out;

import com.bella.backend.domain.usuario.model.StatusUsuario;
import com.bella.backend.domain.usuario.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorLogin(String login);

    boolean existePorLogin(String login);

    List<Usuario> listar(StatusUsuario status);

    long contarAdministradoresAtivos();

    long contar();
}
