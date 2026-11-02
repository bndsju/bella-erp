package com.bella.backend.application.auditoria;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.in.ConsultarAuditoriaUseCase;
import com.bella.backend.domain.auditoria.port.in.RegistrarAuditoriaUseCase;
import com.bella.backend.domain.auditoria.port.out.AuditoriaRepositoryPort;
import com.bella.backend.domain.auditoria.port.out.UsuarioResponsavelPort;
import com.bella.backend.domain.relatorio.model.Pagina;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuditoriaService implements RegistrarAuditoriaUseCase, ConsultarAuditoriaUseCase {

    private final AuditoriaRepositoryPort auditoriaRepositoryPort;
    private final UsuarioResponsavelPort usuarioResponsavelPort;

    public AuditoriaService(AuditoriaRepositoryPort auditoriaRepositoryPort,
                            UsuarioResponsavelPort usuarioResponsavelPort) {
        this.auditoriaRepositoryPort = auditoriaRepositoryPort;
        this.usuarioResponsavelPort = usuarioResponsavelPort;
    }

    /** Participa da transação da operação auditada: se a gravação falhar, a operação é desfeita. */
    @Override
    public RegistroAuditoria registrar(ComandoAuditoria comando) {
        String usuario = comando.usuario() == null || comando.usuario().isBlank()
                ? usuarioResponsavelPort.usuarioAtual()
                : comando.usuario();

        return auditoriaRepositoryPort.salvar(RegistroAuditoria.novo(
                comando.modulo(), comando.entidadeId(), comando.operacao(), usuario,
                comando.valoresAnteriores(), comando.novosValores(), comando.motivo()));
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroAuditoria buscarPorId(UUID id) {
        return auditoriaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Registro de auditoria", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<RegistroAuditoria> historico(ModuloAuditoria modulo, UUID entidadeId, int pagina, int tamanho) {
        return listar(new FiltroAuditoria(null, modulo, null, entidadeId, PeriodoRelatorio.opcional(null, null)),
                pagina, tamanho);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<RegistroAuditoria> listar(FiltroAuditoria filtro, int pagina, int tamanho) {
        Pagina.validar(pagina, tamanho);
        return auditoriaRepositoryPort.buscar(filtro, pagina, tamanho);
    }
}
