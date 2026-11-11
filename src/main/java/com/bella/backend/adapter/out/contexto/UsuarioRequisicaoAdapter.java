package com.bella.backend.adapter.out.contexto;

import com.bella.backend.adapter.in.web.seguranca.UsuarioAutenticado;
import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.out.UsuarioResponsavelPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Responsável pela operação em andamento: o login do usuário autenticado. Fora de uma requisição (rotinas
 * internas) ou sem usuário autenticado, o responsável é "sistema".
 * <p>
 * O cabeçalho {@code X-Usuario} só é aceito com a segurança desligada ({@code bella.seguranca.habilitada=false},
 * usada apenas nos testes); com a segurança ligada ele é ignorado, pois qualquer cliente poderia forjá-lo.
 */
@Component
public class UsuarioRequisicaoAdapter implements UsuarioResponsavelPort {

    public static final String CABECALHO = "X-Usuario";
    public static final String USUARIO_SISTEMA = "sistema";

    private final boolean segurancaHabilitada;

    public UsuarioRequisicaoAdapter(@Value("${bella.seguranca.habilitada:true}") boolean segurancaHabilitada) {
        this.segurancaHabilitada = segurancaHabilitada;
    }

    @Override
    public String usuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioAutenticado autenticado) {
            return limitar(autenticado.login());
        }
        if (segurancaHabilitada) {
            return USUARIO_SISTEMA;
        }
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (!(atributos instanceof ServletRequestAttributes servlet)) {
            return USUARIO_SISTEMA;
        }
        HttpServletRequest requisicao = servlet.getRequest();
        String informado = requisicao.getHeader(CABECALHO);
        if (informado == null) {
            return USUARIO_SISTEMA;
        }
        String limpo = informado.replaceAll("\\p{Cntrl}", "").trim();
        return limpo.isEmpty() ? USUARIO_SISTEMA : limitar(limpo);
    }

    private static String limitar(String valor) {
        return valor.length() > RegistroAuditoria.TAMANHO_MAXIMO_USUARIO
                ? valor.substring(0, RegistroAuditoria.TAMANHO_MAXIMO_USUARIO)
                : valor;
    }
}
