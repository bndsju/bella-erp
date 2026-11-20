package com.bella.backend.adapter.in.web.seguranca;

import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.out.EmissorTokenPort;
import com.bella.backend.domain.usuario.port.out.UsuarioRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Lê o cabeçalho {@code Authorization: Bearer <token>} e autentica a requisição. Perfil e status vêm do banco a
 * cada chamada: inativar um usuário ou trocar a senha dele derruba os tokens antigos na hora. Token ruim não
 * gera erro aqui; a requisição segue anônima e a configuração de segurança responde 401.
 */
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";
    /** Quem entrou com senha provisória só ganha esta autoridade: nada além de trocar a senha é liberado. */
    public static final String PRECISA_TROCAR_SENHA = "PRECISA_TROCAR_SENHA";

    private final EmissorTokenPort emissorTokenPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public FiltroAutenticacaoJwt(EmissorTokenPort emissorTokenPort, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.emissorTokenPort = emissorTokenPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            autenticar(cabecalho.substring(PREFIXO.length()).trim());
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String token) {
        Optional<Usuario> usuario = emissorTokenPort.validar(token)
                .flatMap(dados -> usuarioRepositoryPort.buscarPorId(dados.usuarioId())
                        .filter(Usuario::estaAtivo)
                        // o token guarda segundos inteiros; a senha trocada no mesmo segundo ainda vale
                        .filter(u -> !dados.emitidoEm().isBefore(u.getSenhaAlteradaEm().truncatedTo(ChronoUnit.SECONDS))));
        usuario.ifPresent(u -> {
            UsuarioAutenticado principal = UsuarioAutenticado.de(u);
            var autenticacao = new UsernamePasswordAuthenticationToken(principal, null,
                    List.of(new SimpleGrantedAuthority(
                            u.isSenhaProvisoria() ? PRECISA_TROCAR_SENHA : "ROLE_" + u.getPerfil().name())));
            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        });
    }
}
