package com.bella.backend.application.usuario.usecase;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UsuarioService implements GerenciarUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final CodificadorSenhaPort codificadorSenhaPort;
    private final RegistrarAuditoriaUseCase auditoria;

    public UsuarioService(UsuarioRepositoryPort usuarioRepositoryPort, CodificadorSenhaPort codificadorSenhaPort,
                          RegistrarAuditoriaUseCase auditoria) {
        this.auditoria = auditoria;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.codificadorSenhaPort = codificadorSenhaPort;
    }

    @Override
    public Usuario cadastrar(CadastrarComando comando) {
        String login = Usuario.normalizarLogin(comando.login());
        if (usuarioRepositoryPort.existePorLogin(login)) {
            throw new RegraDeNegocioException("Já existe um usuário com o login \"" + login + "\"");
        }
        Usuario.validarSenhaNova(comando.senha(), login);
        Usuario usuario = Usuario.novo(comando.nome(), login, codificadorSenhaPort.codificar(comando.senha()),
                comando.perfil());
        Usuario salvo = usuarioRepositoryPort.salvar(usuario);
        registrar(salvo, OperacaoAuditoria.CRIACAO, null, dados(salvo), null);
        return salvo;
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
        Map<String, Object> antes = dados(usuario);
        usuario.editar(comando.nome(), comando.perfil());
        Usuario salvo = usuarioRepositoryPort.salvar(usuario);
        registrar(salvo, OperacaoAuditoria.ALTERACAO, antes, dados(salvo), null);
        return salvo;
    }

    @Override
    public Usuario alterarStatus(UUID executorId, UUID id, StatusUsuario novoStatus) {
        Usuario usuario = buscarPorId(id);
        Map<String, Object> antes = dados(usuario);
        if (novoStatus == StatusUsuario.ATIVO) {
            usuario.ativar();
        } else {
            if (id.equals(executorId)) {
                throw new RegraDeNegocioException("Você não pode inativar o seu próprio usuário");
            }
            exigirOutroAdministradorAtivo(usuario);
            usuario.inativar();
        }
        Usuario salvo = usuarioRepositoryPort.salvar(usuario);
        registrar(salvo, novoStatus == StatusUsuario.ATIVO ? OperacaoAuditoria.REATIVACAO : OperacaoAuditoria.INATIVACAO,
                antes, dados(salvo), null);
        return salvo;
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
        registrar(usuario, OperacaoAuditoria.SENHA_PROVISORIA, null, null, "Senha provisória gerada pelo administrador");
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

    /** Só nome, login, perfil e status: nunca a senha. */
    private static Map<String, Object> dados(Usuario usuario) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", usuario.getNome());
        dados.put("login", usuario.getLogin());
        dados.put("perfil", usuario.getPerfil().name());
        dados.put("status", usuario.getStatus().name());
        return dados;
    }

    private void registrar(Usuario usuario, OperacaoAuditoria operacao, Map<String, Object> antes,
                           Map<String, Object> depois, String motivo) {
        auditoria.registrar(new RegistrarAuditoriaUseCase.ComandoAuditoria(
                ModuloAuditoria.USUARIO, usuario.getId(), operacao, null, antes, depois, motivo));
    }

    /** Se o usuário é um administrador ativo, precisa sobrar outro; senão ninguém mais gerenciaria os acessos. */
    private void exigirOutroAdministradorAtivo(Usuario usuario) {
        if (usuario.ehAdministradorAtivo() && usuarioRepositoryPort.contarAdministradoresAtivos() <= 1) {
            throw new RegraDeNegocioException("O sistema precisa de pelo menos um administrador ativo");
        }
    }
}
