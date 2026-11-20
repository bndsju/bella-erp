package com.bella.backend.domain.usuario.port.in;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import com.bella.backend.domain.usuario.model.Usuario;

import java.util.List;
import java.util.UUID;

/** Administração de usuários. O {@code executorId} é quem faz a operação, para impedir que alguém se tranque fora do sistema. */
public interface GerenciarUsuariosUseCase {

    Usuario cadastrar(CadastrarComando comando);

    Usuario editar(UUID executorId, UUID id, EditarComando comando);

    Usuario alterarStatus(UUID executorId, UUID id, StatusUsuario novoStatus);

    /**
     * Gera uma senha aleatória para a pessoa (atende o "esqueci a senha"), devolvida uma única vez para o
     * administrador repassar. No próximo login a pessoa é obrigada a trocá-la.
     */
    String gerarSenhaProvisoria(UUID executorId, UUID id);

    Usuario buscarPorId(UUID id);

    List<Usuario> listar(StatusUsuario status);

    record CadastrarComando(String nome, String login, String senha, PerfilUsuario perfil) {
    }

    record EditarComando(String nome, PerfilUsuario perfil) {
    }
}
