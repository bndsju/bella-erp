package com.bella.backend.adapter.out.contexto;

import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.out.UsuarioResponsavelPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Usuário da requisição HTTP em andamento, informado no cabeçalho {@code X-Usuario}. Sem autenticação
 * no projeto, o cabeçalho é apenas declaratório. Fora de uma requisição (rotinas internas) ou sem o
 * cabeçalho, o responsável é "sistema".
 */
@Component
public class UsuarioRequisicaoAdapter implements UsuarioResponsavelPort {

    public static final String CABECALHO = "X-Usuario";
    public static final String USUARIO_SISTEMA = "sistema";

    @Override
    public String usuarioAtual() {
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
        if (limpo.isEmpty()) {
            return USUARIO_SISTEMA;
        }
        return limpo.length() > RegistroAuditoria.TAMANHO_MAXIMO_USUARIO
                ? limpo.substring(0, RegistroAuditoria.TAMANHO_MAXIMO_USUARIO)
                : limpo;
    }
}
