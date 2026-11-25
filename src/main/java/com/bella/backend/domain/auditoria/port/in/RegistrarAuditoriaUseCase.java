package com.bella.backend.domain.auditoria.port.in;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.model.RegistroAuditoria;

import java.util.Map;
import java.util.UUID;

/**
 * Porta de uso interno: a auditoria é criada pelo próprio sistema, nunca por um endpoint. Sem
 * {@code usuario} informado, usa o usuário da requisição em andamento.
 */
public interface RegistrarAuditoriaUseCase {

    RegistroAuditoria registrar(ComandoAuditoria comando);

    /**
     * Grava numa transação própria, que não é desfeita se a operação de origem falhar. Serve para eventos de
     * segurança, como o login recusado, que terminam em erro mas precisam ficar registrados.
     */
    RegistroAuditoria registrarIndependente(ComandoAuditoria comando);

    record ComandoAuditoria(
            ModuloAuditoria modulo,
            UUID entidadeId,
            OperacaoAuditoria operacao,
            String usuario,
            Map<String, Object> valoresAnteriores,
            Map<String, Object> novosValores,
            String motivo
    ) {
    }
}
