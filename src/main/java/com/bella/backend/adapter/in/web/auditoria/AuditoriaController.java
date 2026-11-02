package com.bella.backend.adapter.in.web.auditoria;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.in.ConsultarAuditoriaUseCase;
import com.bella.backend.domain.auditoria.port.in.ConsultarAuditoriaUseCase.FiltroAuditoria;
import com.bella.backend.domain.relatorio.model.Pagina;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Consulta da auditoria. Só há leitura: os registros são criados automaticamente pelo sistema e não
 * existem endpoints para criar, alterar ou excluir.
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final ConsultarAuditoriaUseCase consultarAuditoriaUseCase;

    public AuditoriaController(ConsultarAuditoriaUseCase consultarAuditoriaUseCase) {
        this.consultarAuditoriaUseCase = consultarAuditoriaUseCase;
    }

    @GetMapping
    public Pagina<RegistroAuditoria> listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) ModuloAuditoria modulo,
            @RequestParam(required = false) OperacaoAuditoria operacao,
            @RequestParam(required = false) UUID entidadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarAuditoriaUseCase.listar(
                new FiltroAuditoria(usuario, modulo, operacao, entidadeId, PeriodoRelatorio.opcional(inicio, fim)),
                pagina, tamanho);
    }

    @GetMapping("/{id}")
    public RegistroAuditoria buscarPorId(@PathVariable UUID id) {
        return consultarAuditoriaUseCase.buscarPorId(id);
    }

    @GetMapping("/historico/{modulo}/{entidadeId}")
    public Pagina<RegistroAuditoria> historico(
            @PathVariable ModuloAuditoria modulo,
            @PathVariable UUID entidadeId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarAuditoriaUseCase.historico(modulo, entidadeId, pagina, tamanho);
    }
}
