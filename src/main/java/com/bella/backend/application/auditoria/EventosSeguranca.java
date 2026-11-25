package com.bella.backend.application.auditoria;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase.ComandoAuditoria;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Atalhos para registrar na auditoria o que importa para a segurança: logins, bloqueios e mudanças de senha.
 * Os registros são gravados em transação própria, então ficam mesmo quando a operação termina em erro.
 * Nunca recebem senha ou token.
 */
@Component
public class EventosSeguranca {

    private final RegistrarAuditoriaUseCase registrar;

    public EventosSeguranca(RegistrarAuditoriaUseCase registrar) {
        this.registrar = registrar;
    }

    public void loginSucesso(UUID usuarioId, String login, String ip) {
        gravar(OperacaoAuditoria.LOGIN_SUCESSO, usuarioId, login, ip, null);
    }

    /** {@code usuarioId} é nulo quando o login informado não existe; o registro fica ligado ao texto digitado. */
    public void loginFalha(UUID usuarioId, String loginDigitado, String ip, String motivo) {
        gravar(OperacaoAuditoria.LOGIN_FALHA, usuarioId, loginDigitado, ip, motivo);
    }

    public void loginBloqueado(UUID usuarioId, String loginDigitado, String ip, long minutos) {
        gravar(OperacaoAuditoria.LOGIN_BLOQUEADO, usuarioId, loginDigitado, ip,
                "Login bloqueado por " + minutos + " minutos após várias tentativas erradas");
    }

    public void limiteExcedido(String rota, String ip) {
        gravar(OperacaoAuditoria.LIMITE_EXCEDIDO, null, "ip:" + ip, ip, "Muitas requisições em " + rota);
    }

    public void redefinicaoSolicitada(UUID usuarioId, String login, String ip) {
        gravar(OperacaoAuditoria.REDEFINICAO_SOLICITADA, usuarioId, login, ip, null);
    }

    private void gravar(OperacaoAuditoria operacao, UUID usuarioId, String quem, String ip, String motivo) {
        String nome = limitar(quem == null || quem.isBlank() ? "(vazio)" : quem.replaceAll("\\p{Cntrl}", " ").trim());
        UUID entidade = usuarioId != null ? usuarioId
                : UUID.nameUUIDFromBytes(("login:" + nome.toLowerCase()).getBytes(StandardCharsets.UTF_8));
        Map<String, Object> detalhes = new LinkedHashMap<>();
        detalhes.put("login", nome);
        if (ip != null) {
            detalhes.put("ip", limitar(ip));
        }
        registrar.registrarIndependente(new ComandoAuditoria(
                ModuloAuditoria.SEGURANCA, entidade, operacao, nome, null, detalhes, motivo));
    }

    private static String limitar(String valor) {
        return valor.length() > RegistroAuditoria.TAMANHO_MAXIMO_USUARIO
                ? valor.substring(0, RegistroAuditoria.TAMANHO_MAXIMO_USUARIO)
                : valor;
    }
}
