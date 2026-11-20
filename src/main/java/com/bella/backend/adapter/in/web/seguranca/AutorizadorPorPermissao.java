package com.bella.backend.adapter.in.web.seguranca;

import com.bella.backend.domain.permissao.model.RequisicaoClassificada;
import com.bella.backend.domain.permissao.port.in.ConsultarPermissoesUseCase;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Decide cada chamada da API pelas permissões do perfil do usuário: descobre o módulo e a ação pelo método e pelo
 * caminho e consulta a matriz configurada pelo administrador. O administrador passa sempre. Caminho que não
 * pertence a nenhum módulo é negado a todos os outros perfis. Quem está com senha provisória não tem perfil
 * efetivo (só a autoridade de trocar a senha) e portanto é negado aqui.
 */
public class AutorizadorPorPermissao implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String PREFIXO_PERFIL = "ROLE_";

    private final ConsultarPermissoesUseCase permissoes;

    public AutorizadorPorPermissao(ConsultarPermissoesUseCase permissoes) {
        this.permissoes = permissoes;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> autenticacao, RequestAuthorizationContext contexto) {
        Authentication atual = autenticacao.get();
        if (atual == null || !atual.isAuthenticated() || atual instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }
        Optional<PerfilUsuario> perfil = perfilDe(atual);
        if (perfil.isEmpty()) {
            return new AuthorizationDecision(false);
        }
        if (perfil.get() == PerfilUsuario.ADMINISTRADOR) {
            return new AuthorizationDecision(true);
        }
        HttpServletRequest requisicao = contexto.getRequest();
        String caminho = requisicao.getRequestURI().substring(requisicao.getContextPath().length());
        boolean permitido = RequisicaoClassificada.de(requisicao.getMethod(), caminho)
                .map(c -> permissoes.permite(perfil.get(), c.modulo(), c.acao()))
                .orElse(false);
        return new AuthorizationDecision(permitido);
    }

    private static Optional<PerfilUsuario> perfilDe(Authentication autenticacao) {
        for (GrantedAuthority autoridade : autenticacao.getAuthorities()) {
            String nome = autoridade.getAuthority();
            if (nome.startsWith(PREFIXO_PERFIL)) {
                try {
                    return Optional.of(PerfilUsuario.valueOf(nome.substring(PREFIXO_PERFIL.length())));
                } catch (IllegalArgumentException ignorada) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}
