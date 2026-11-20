package com.bella.backend.application.usuario.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
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

    /** Falhas por login, só em memória: some ao reiniciar, o que basta para frear tentativa de adivinhar senha. */
    private final Map<String, Falhas> falhasPorLogin = new ConcurrentHashMap<>();
    /** Hash de uma senha qualquer, usado para gastar o mesmo tempo quando o login não existe. */
    private final String hashFalso;

    public AutenticacaoService(UsuarioRepositoryPort usuarioRepositoryPort, CodificadorSenhaPort codificadorSenhaPort,
                               EmissorTokenPort emissorTokenPort) {
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
            registrarFalha(chave);
            throw new CredenciaisInvalidasException();
        }

        falhasPorLogin.remove(chave);
        Usuario usuario = encontrado.get();
        usuario.registrarAcesso();
        usuarioRepositoryPort.salvar(usuario);
        EmissorTokenPort.TokenEmitido token = emissorTokenPort.emitir(usuario);
        return new Sessao(token.token(), token.expiraEm(), usuario);
    }

    @Override
    public void alterar(UUID usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuario", usuarioId));
        if (senhaAtual == null || !codificadorSenhaPort.confere(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("A senha atual não confere");
        }
        Usuario.validarSenhaNova(novaSenha);
        if (novaSenha.equals(senhaAtual)) {
            throw new RegraDeNegocioException("A nova senha deve ser diferente da atual");
        }
        usuario.trocarSenha(codificadorSenhaPort.codificar(novaSenha));
        usuarioRepositoryPort.salvar(usuario);
    }

    @Override
    public void solicitar(String login) {
        String chave = login == null ? "" : login.trim().toLowerCase();
        if (chave.isEmpty()) {
            return;
        }
        usuarioRepositoryPort.buscarPorLogin(chave).ifPresent(usuario -> {
            if (usuario.estaAtivo() && usuario.getRedefinicaoSolicitadaEm() == null) {
                usuario.solicitarRedefinicao();
                usuarioRepositoryPort.salvar(usuario);
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

    private void registrarFalha(String chave) {
        falhasPorLogin.compute(chave, (k, atual) -> {
            Falhas falhas = atual == null || atual.bloqueadoAte != null ? new Falhas() : atual;
            falhas.quantidade++;
            if (falhas.quantidade >= MAXIMO_TENTATIVAS) {
                falhas.bloqueadoAte = Instant.now().plus(BLOQUEIO);
            }
            return falhas;
        });
    }

    private static final class Falhas {
        int quantidade;
        Instant bloqueadoAte;
    }
}
