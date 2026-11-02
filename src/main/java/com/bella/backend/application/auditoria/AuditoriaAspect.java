package com.bella.backend.application.auditoria;

import com.bella.backend.application.auditoria.SnapshotAuditoria.Diferenca;
import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase.ComandoAuditoria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.RecordComponent;
import java.util.UUID;

/**
 * Único ponto onde as regras de auditoria são aplicadas. Roda dentro da transação do serviço
 * auditado (ver {@code TransacaoConfig}), de modo que o registro e a operação são confirmados ou
 * desfeitos juntos. As dependências são resolvidas sob demanda para o aspecto não ser criado antes
 * dos serviços que ele intercepta.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditoriaAspect {

    private final ObjectProvider<CarregadoresAuditoria> carregadores;
    private final ObjectProvider<RegistrarAuditoriaUseCase> registrarAuditoria;
    private final SnapshotAuditoria snapshots;

    public AuditoriaAspect(ObjectProvider<CarregadoresAuditoria> carregadores,
                           ObjectProvider<RegistrarAuditoriaUseCase> registrarAuditoria,
                           SnapshotAuditoria snapshots) {
        this.carregadores = carregadores;
        this.registrarAuditoria = registrarAuditoria;
        this.snapshots = snapshots;
    }

    @Around("@annotation(auditado)")
    public Object auditar(ProceedingJoinPoint joinPoint, Auditado auditado) throws Throwable {
        ModuloAuditoria modulo = auditado.modulo();
        OperacaoAuditoria operacao = auditado.operacao();
        boolean cria = operacao == OperacaoAuditoria.CRIACAO || operacao == OperacaoAuditoria.MOVIMENTACAO_ESTOQUE;

        UUID id = null;
        ObjectNode antes = null;
        if (!cria) {
            id = primeiroIdentificador(joinPoint);
            antes = snapshots.paraNo(carregadores.getObject().carregar(modulo, id));
        }

        Object resultado = joinPoint.proceed();

        ObjectNode depois = resultado != null ? snapshots.paraNo(resultado) : null;
        if (cria) {
            id = identificadorDe(depois, joinPoint);
        } else if (depois == null || !id.toString().equals(textoDe(depois, "id"))) {
            depois = snapshots.paraNo(carregadores.getObject().carregar(modulo, id));
        }

        Diferenca diferenca = cria
                ? new Diferenca(null, snapshots.paraMapa(depois))
                : snapshots.diferenca(antes, depois);
        if (!cria && operacao == OperacaoAuditoria.ALTERACAO && diferenca.vazia()) {
            return resultado;
        }

        registrarAuditoria.getObject().registrar(new ComandoAuditoria(
                modulo, id, refinar(operacao, antes, depois), extrair(joinPoint, "usuarioResponsavel"),
                diferenca.anteriores(), diferenca.novos(), extrair(joinPoint, "motivo")));
        return resultado;
    }

    /** Mudança de status ATIVO/INATIVO vira inativação ou reativação, que são operações próprias. */
    private OperacaoAuditoria refinar(OperacaoAuditoria operacao, ObjectNode antes, ObjectNode depois) {
        if (operacao != OperacaoAuditoria.ALTERACAO_STATUS || antes == null || depois == null) {
            return operacao;
        }
        String statusAntes = textoDe(antes, "status");
        String statusDepois = textoDe(depois, "status");
        if ("INATIVO".equals(statusDepois) && !"INATIVO".equals(statusAntes)) {
            return OperacaoAuditoria.INATIVACAO;
        }
        if ("ATIVO".equals(statusDepois) && "INATIVO".equals(statusAntes)) {
            return OperacaoAuditoria.REATIVACAO;
        }
        return operacao;
    }

    private UUID primeiroIdentificador(ProceedingJoinPoint joinPoint) {
        for (Object argumento : joinPoint.getArgs()) {
            if (argumento instanceof UUID uuid) {
                return uuid;
            }
        }
        throw new IllegalStateException("Método auditado sem identificador do registro: "
                + joinPoint.getSignature().toShortString());
    }

    private UUID identificadorDe(ObjectNode snapshot, ProceedingJoinPoint joinPoint) {
        String id = snapshot == null ? null : textoDe(snapshot, "id");
        if (id == null) {
            throw new IllegalStateException("Método auditado não retornou o registro criado: "
                    + joinPoint.getSignature().toShortString());
        }
        return UUID.fromString(id);
    }

    private static String textoDe(JsonNode no, String campo) {
        JsonNode valor = no.get(campo);
        return valor == null || valor.isNull() ? null : valor.asText();
    }

    /**
     * Procura um texto pelo nome, primeiro entre os parâmetros do método e depois entre os campos de
     * argumentos do tipo record (os comandos): é assim que se obtém o motivo e o usuário responsável.
     */
    private String extrair(ProceedingJoinPoint joinPoint, String nome) {
        Object[] argumentos = joinPoint.getArgs();
        String[] parametros = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (parametros != null) {
            for (int i = 0; i < parametros.length; i++) {
                if (nome.equals(parametros[i]) && argumentos[i] instanceof String texto) {
                    return texto;
                }
            }
        }
        for (Object argumento : argumentos) {
            if (argumento == null || !argumento.getClass().isRecord()) {
                continue;
            }
            for (RecordComponent componente : argumento.getClass().getRecordComponents()) {
                if (nome.equals(componente.getName()) && componente.getType() == String.class) {
                    try {
                        return (String) componente.getAccessor().invoke(argumento);
                    } catch (ReflectiveOperationException erro) {
                        throw new IllegalStateException("Não foi possível ler " + nome + " do comando", erro);
                    }
                }
            }
        }
        return null;
    }
}
