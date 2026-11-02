package com.bella.backend.domain.auditoria.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registro imutável de uma operação realizada no sistema. Não existe operação de edição nem de
 * exclusão: o registro é criado uma única vez, automaticamente, e só pode ser consultado.
 *
 * <p>{@code valoresAnteriores} e {@code novosValores} guardam apenas os campos que mudaram (ou o
 * estado completo, na criação). Nunca devem conter dados sensíveis como senhas ou tokens.
 */
public record RegistroAuditoria(
        UUID id,
        ModuloAuditoria modulo,
        UUID entidadeId,
        OperacaoAuditoria operacao,
        String usuario,
        Instant dataHora,
        Map<String, Object> valoresAnteriores,
        Map<String, Object> novosValores,
        String motivo
) {

    public static final int TAMANHO_MAXIMO_USUARIO = 100;
    public static final int TAMANHO_MAXIMO_MOTIVO = 1000;

    public RegistroAuditoria {
        if (id == null || dataHora == null) {
            throw new RegraDeNegocioException("Identificador e data da auditoria são obrigatórios");
        }
        if (modulo == null) {
            throw new RegraDeNegocioException("Módulo da auditoria é obrigatório");
        }
        if (entidadeId == null) {
            throw new RegraDeNegocioException("Registro afetado é obrigatório na auditoria");
        }
        if (operacao == null) {
            throw new RegraDeNegocioException("Operação da auditoria é obrigatória");
        }
        usuario = usuario == null ? null : usuario.trim();
        if (usuario == null || usuario.isEmpty()) {
            throw new RegraDeNegocioException("Usuário responsável é obrigatório na auditoria");
        }
        if (usuario.length() > TAMANHO_MAXIMO_USUARIO) {
            throw new RegraDeNegocioException("Usuário responsável deve ter no máximo "
                    + TAMANHO_MAXIMO_USUARIO + " caracteres");
        }
        motivo = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (motivo != null && motivo.length() > TAMANHO_MAXIMO_MOTIVO) {
            motivo = motivo.substring(0, TAMANHO_MAXIMO_MOTIVO);
        }
        valoresAnteriores = copiaImutavel(valoresAnteriores);
        novosValores = copiaImutavel(novosValores);
    }

    public static RegistroAuditoria novo(ModuloAuditoria modulo, UUID entidadeId, OperacaoAuditoria operacao,
                                         String usuario, Map<String, Object> valoresAnteriores,
                                         Map<String, Object> novosValores, String motivo) {
        return new RegistroAuditoria(UUID.randomUUID(), modulo, entidadeId, operacao, usuario, Instant.now(),
                valoresAnteriores, novosValores, motivo);
    }

    private static Map<String, Object> copiaImutavel(Map<String, Object> valores) {
        return valores == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(valores));
    }
}
