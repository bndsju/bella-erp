package com.bella.backend.domain.auditoria.port.in;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.relatorio.model.Pagina;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;

import java.util.UUID;

/** Consultas de auditoria, sempre da mais recente para a mais antiga. */
public interface ConsultarAuditoriaUseCase {

    RegistroAuditoria buscarPorId(UUID id);

    /** Histórico de um registro específico. */
    Pagina<RegistroAuditoria> historico(ModuloAuditoria modulo, UUID entidadeId, int pagina, int tamanho);

    Pagina<RegistroAuditoria> listar(FiltroAuditoria filtro, int pagina, int tamanho);

    /** Todos os filtros são opcionais; o período é inclusivo nas duas pontas. */
    record FiltroAuditoria(String usuario, ModuloAuditoria modulo, OperacaoAuditoria operacao, UUID entidadeId,
                           PeriodoRelatorio periodo) {
    }
}
