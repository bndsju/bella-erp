package com.bella.backend.application.usuario.usecase;

import com.bella.backend.application.auditoria.EventosSeguranca;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.usuario.model.CredenciaisInvalidasException;
import com.bella.backend.domain.usuario.model.LoginBloqueadoException;
import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.in.AlterarMinhaSenhaUseCase;
import com.bella.backend.domain.usuario.port.in.AutenticarUseCase;
import com.bella.backend.domain.usuario.port.in.SolicitarRedefinicaoSenhaUseCase;
import com.bella.backend.domain.usuario.port.out.CodificadorSenhaPort;
import com.bella.backend.domain.usuario.port.out.EmissorTokenPort;
import com.bella.backend.domain.usuario.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AutenticacaoService
        implements AutenticarUseCase, AlterarMinhaSenhaUseCase, SolicitarRedefinicaoSenhaUseCase {

    static final int MAXIMO_TENTATIVAS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final CodificadorSenhaPort codificadorSenhaPort;
    private final EmissorTokenPort emissorTokenPort;
    private final EventosSeguranca eventosSeguranca;
    private final RegistrarAuditoriaUseCase registrarAuditoriaUseCase;

    /** Falhas por login, só em memória: some ao reiniciar, o que basta para frear tentativa de adivinhar senha. */
    private final Map<String, Falhas> falhasPorLogin = new ConcurrentHashMap<>();
    /** Hash de uma senha qualquer, usado para gastar o mesmo tempo quando o login não existe. */
    private final String hashFalso;

    public AutenticacaoService(UsuarioRepositoryPort usuarioRepositoryPort, CodificadorSenhaPort codificadorSenhaPort,
                               EmissorTokenPort emissorTokenPort, EventosSeguranca eventosSeguranca,
                               RegistrarAuditoriaUseCase registrarAuditoriaUseCase) {
        this.eventosSeguranca = eventosSeguranca;
        this.registrarAuditoriaUseCase = registrarAuditoriaUseCase;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.codificadorSenhaPort = codificadorSenhaPort;
        this.emissorTokenPort = emissorTokenPort;
        this.hashFalso = codificadorSenhaPort.codificar(UUID.randomUUID().toString());
    }

    @Override
    public Sessao autenticar(Comando comando) {
        String chave = comando.login() == null ? "" : comando.login().trim().toLowerCase();
        exigirNaoBloqueado(chave);

        Optional<Usuario> encontrado = chave.isEmpty() ? Optional.empty() : usuarioRepositoryPort.buscarPorLogin(chave);
        String senha = comando.senha() == null ? "" : comando.senha();
        boolean senhaConfere = codificadorSenhaPort.confere(senha, encontrado.map(Usuario::getSenhaHash).orElse(hashFalso));

        if (encontrado.isEmpty() || !senhaConfere || !encontrado.get().estaAtivo()) {
            String motivo = encontrado.isEmpty() ? "Login inexistente"
                    : !encontrado.get().estaAtivo() ? "Usuário inativo" : "Senha incorreta";
            UUID usuarioId = encontrado.map(Usuario::getId).orElse(null);
            eventosSeguranca.loginFalha(usuarioId, chave, comando.enderecoIp(), motivo);
            if (registrarFalha(chave)) {
                eventosSeguranca.loginBloqueado(usuarioId, chave, comando.enderecoIp(), BLOQUEIO.toMinutes());
            }
            throw new CredenciaisInvalidasException();
        }

        falhasPorLogin.remove(chave);
        Usuario usuario = encontrado.get();
        usuario.registrarAcesso();
        usuarioRepositoryPort.salvar(usuario);
        EmissorTokenPort.TokenEmitido token = emissorTokenPort.emitir(usuario);
        eventosSeguranca.loginSucesso(usuario.getId(), usuario.getLogin(), comando.enderecoIp());
        return new Sessao(token.token(), token.expiraEm(), usuario);
    }

    @Override
    public void alterar(UUID usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuario", usuarioId));
        if (senhaAtual == null || !codificadorSenhaPort.confere(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("A senha atual não confere");
        }
        Usuario.validarSenhaNova(novaSenha, usuario.getLogin());
        if (novaSenha.equals(senhaAtual)) {
            throw new RegraDeNegocioException("A nova senha deve ser diferente da atual");
        }
        usuario.trocarSenha(codificadorSenhaPort.codificar(novaSenha));
        usuarioRepositoryPort.salvar(usuario);
        registrarAuditoriaUseCase.registrar(new RegistrarAuditoriaUseCase.ComandoAuditoria(
                ModuloAuditoria.USUARIO, usuario.getId(), OperacaoAuditoria.SENHA_ALTERADA, usuario.getLogin(),
                null, null, "O próprio usuário trocou a senha"));
    }

    @Override
    public void solicitar(String login) {
        solicitar(login, null);
    }

    @Override
    public void solicitar(String login, String enderecoIp) {
        String chave = login == null ? "" : login.trim().toLowerCase();
        if (chave.isEmpty()) {
            return;
        }
        usuarioRepositoryPort.buscarPorLogin(chave).ifPresent(usuario -> {
            if (usuario.estaAtivo() && usuario.getRedefinicaoSolicitadaEm() == null) {
                usuario.solicitarRedefinicao();
                usuarioRepositoryPort.salvar(usuario);
                eventosSeguranca.redefinicaoSolicitada(usuario.getId(), usuario.getLogin(), enderecoIp);
            }
        });
    }

    private void exigirNaoBloqueado(String chave) {
        Falhas falhas = falhasPorLogin.get(chave);
        if (falhas == null) {
            return;
        }
        Instant agora = Instant.now();
        if (falhas.bloqueadoAte != null && agora.isBefore(falhas.bloqueadoAte)) {
            long minutos = Math.max(1, Duration.between(agora, falhas.bloqueadoAte).plusSeconds(59).toMinutes());
            throw new LoginBloqueadoException(minutos);
        }
        if (falhas.bloqueadoAte != null) {
            falhasPorLogin.remove(chave);
        }
    }

    /** Devolve true quando esta falha foi a que bloqueou o login. */
    private boolean registrarFalha(String chave) {
        boolean[] bloqueou = {false};
        falhasPorLogin.compute(chave, (k, atual) -> {
            Falhas falhas = atual == null || atual.bloqueadoAte != null ? new Falhas() : atual;
            falhas.quantidade++;
            if (falhas.quantidade >= MAXIMO_TENTATIVAS) {
                falhas.bloqueadoAte = Instant.now().plus(BLOQUEIO);
                bloqueou[0] = true;
            }
            return falhas;
        });
        return bloqueou[0];
    }

    private static final class Falhas {
        int quantidade;
        Instant bloqueadoAte;
    }
}
