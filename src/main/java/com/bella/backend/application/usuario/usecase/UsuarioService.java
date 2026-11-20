package com.bella.backend.application.usuario.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.SenhaProvisoria;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.in.GerenciarUsuariosUseCase;
import com.bella.backend.domain.usuario.port.out.CodificadorSenhaPort;
import com.bella.backend.domain.usuario.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UsuarioService implements GerenciarUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final CodificadorSenhaPort codificadorSenhaPort;

    public UsuarioService(UsuarioRepositoryPort usuarioRepositoryPort, CodificadorSenhaPort codificadorSenhaPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.codificadorSenhaPort = codificadorSenhaPort;
    }

    @Override
    public Usuario cadastrar(CadastrarComando comando) {
        String login = Usuario.normalizarLogin(comando.login());
        if (usuarioRepositoryPort.existePorLogin(login)) {
            throw new RegraDeNegocioException("Já existe um usuário com o login \"" + login + "\"");
        }
        Usuario.validarSenhaNova(comando.senha());
        Usuario usuario = Usuario.novo(comando.nome(), login, codificadorSenhaPort.codificar(comando.senha()),
                comando.perfil());
        return usuarioRepositoryPort.salvar(usuario);
    }

    @Override
    public Usuario editar(UUID executorId, UUID id, EditarComando comando) {
        Usuario usuario = buscarPorId(id);
        boolean deixaDeSerAdministrador = usuario.getPerfil() == PerfilUsuario.ADMINISTRADOR
                && comando.perfil() != PerfilUsuario.ADMINISTRADOR;
        if (deixaDeSerAdministrador) {
            if (id.equals(executorId)) {
                throw new RegraDeNegocioException("Você não pode tirar o seu próprio perfil de administrador");
            }
            exigirOutroAdministradorAtivo(usuario);
        }
        usuario.editar(comando.nome(), comando.perfil());
        return usuarioRepositoryPort.salvar(usuario);
    }

    @Override
    public Usuario alterarStatus(UUID executorId, UUID id, StatusUsuario novoStatus) {
        Usuario usuario = buscarPorId(id);
        if (novoStatus == StatusUsuario.ATIVO) {
            usuario.ativar();
        } else {
            if (id.equals(executorId)) {
                throw new RegraDeNegocioException("Você não pode inativar o seu próprio usuário");
            }
            exigirOutroAdministradorAtivo(usuario);
            usuario.inativar();
        }
        return usuarioRepositoryPort.salvar(usuario);
    }

    @Override
    public String gerarSenhaProvisoria(UUID executorId, UUID id) {
        if (id.equals(executorId)) {
            throw new RegraDeNegocioException("Para trocar a sua própria senha use \"Minha conta\"");
        }
        Usuario usuario = buscarPorId(id);
        if (!usuario.estaAtivo()) {
            throw new RegraDeNegocioException("Ative o usuário antes de gerar uma senha provisória");
        }
        String senha = SenhaProvisoria.gerar();
        usuario.definirSenhaProvisoria(codificadorSenhaPort.codificar(senha));
        usuarioRepositoryPort.salvar(usuario);
        return senha;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuario", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar(StatusUsuario status) {
        return usuarioRepositoryPort.listar(status);
    }

    /** Se o usuário é um administrador ativo, precisa sobrar outro; senão ninguém mais gerenciaria os acessos. */
    private void exigirOutroAdministradorAtivo(Usuario usuario) {
        if (usuario.ehAdministradorAtivo() && usuarioRepositoryPort.contarAdministradoresAtivos() <= 1) {
            throw new RegraDeNegocioException("O sistema precisa de pelo menos um administrador ativo");
        }
    }
}
