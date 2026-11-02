package com.bella.backend.domain.auditoria.port.out;

import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.in.ConsultarAuditoriaUseCase.FiltroAuditoria;
import com.bella.backend.domain.relatorio.model.Pagina;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório somente de inclusão e consulta. Propositalmente não há operações de atualização nem de
 * exclusão: a imutabilidade também é garantida no banco, por gatilhos.
 */
public interface AuditoriaRepositoryPort {

    RegistroAuditoria salvar(RegistroAuditoria registro);

    Optional<RegistroAuditoria> buscarPorId(UUID id);

    Pagina<RegistroAuditoria> buscar(FiltroAuditoria filtro, int pagina, int tamanho);
}
