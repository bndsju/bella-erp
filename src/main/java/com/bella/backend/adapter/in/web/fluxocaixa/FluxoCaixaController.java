package com.bella.backend.adapter.in.web.fluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.in.BuscarCategoriaFluxoCaixaPorIdUseCase;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.BuscarMovimentacaoCaixaPorIdUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.CancelarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ConsultarFluxoCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.EditarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.LancarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ListarMovimentacoesCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ListarMovimentacoesCaixaUseCase.FiltroListagem;
import com.bella.backend.domain.fluxocaixa.port.in.RealizarMovimentacaoManualCaixaUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fluxo-caixa")
public class FluxoCaixaController {

    private final LancarMovimentacaoManualCaixaUseCase lancarMovimentacaoManualCaixaUseCase;
    private final EditarMovimentacaoManualCaixaUseCase editarMovimentacaoManualCaixaUseCase;
    private final RealizarMovimentacaoManualCaixaUseCase realizarMovimentacaoManualCaixaUseCase;
    private final CancelarMovimentacaoManualCaixaUseCase cancelarMovimentacaoManualCaixaUseCase;
    private final BuscarMovimentacaoCaixaPorIdUseCase buscarMovimentacaoCaixaPorIdUseCase;
    private final ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase;
    private final ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase;
    private final BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase;

    public FluxoCaixaController(
            LancarMovimentacaoManualCaixaUseCase lancarMovimentacaoManualCaixaUseCase,
            EditarMovimentacaoManualCaixaUseCase editarMovimentacaoManualCaixaUseCase,
            RealizarMovimentacaoManualCaixaUseCase realizarMovimentacaoManualCaixaUseCase,
            CancelarMovimentacaoManualCaixaUseCase cancelarMovimentacaoManualCaixaUseCase,
            BuscarMovimentacaoCaixaPorIdUseCase buscarMovimentacaoCaixaPorIdUseCase,
            ListarMovimentacoesCaixaUseCase listarMovimentacoesCaixaUseCase,
            ConsultarFluxoCaixaUseCase consultarFluxoCaixaUseCase,
            BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase
    ) {
        this.lancarMovimentacaoManualCaixaUseCase = lancarMovimentacaoManualCaixaUseCase;
        this.editarMovimentacaoManualCaixaUseCase = editarMovimentacaoManualCaixaUseCase;
        this.realizarMovimentacaoManualCaixaUseCase = realizarMovimentacaoManualCaixaUseCase;
        this.cancelarMovimentacaoManualCaixaUseCase = cancelarMovimentacaoManualCaixaUseCase;
        this.buscarMovimentacaoCaixaPorIdUseCase = buscarMovimentacaoCaixaPorIdUseCase;
        this.listarMovimentacoesCaixaUseCase = listarMovimentacoesCaixaUseCase;
        this.consultarFluxoCaixaUseCase = consultarFluxoCaixaUseCase;
        this.buscarCategoriaFluxoCaixaPorIdUseCase = buscarCategoriaFluxoCaixaPorIdUseCase;
    }

    @PostMapping("/movimentacoes")
    public ResponseEntity<MovimentacaoCaixaResponse> lancar(@Valid @RequestBody LancamentoManualCaixaRequest request) {
        MovimentacaoCaixa movimentacao = lancarMovimentacaoManualCaixaUseCase.lancar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/fluxo-caixa/movimentacoes/" + movimentacao.getId()))
                .body(paraResponse(movimentacao));
    }

    @GetMapping("/movimentacoes/{id}")
    public MovimentacaoCaixaResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarMovimentacaoCaixaPorIdUseCase.buscarPorId(id));
    }

    @GetMapping("/movimentacoes")
    public List<MovimentacaoCaixaResponse> listar(
            @RequestParam(required = false) TipoMovimentacaoCaixa tipo,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) OrigemMovimentacaoCaixa origem,
            @RequestParam(required = false) SituacaoMovimentacaoCaixa situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "false") boolean incluirCanceladas) {
        FiltroListagem filtro = new FiltroListagem(tipo, categoriaId, origem, situacao, dataInicio, dataFim,
                incluirCanceladas);
        return listarMovimentacoesCaixaUseCase.listar(filtro).stream().map(this::paraResponse).toList();
    }

    @PutMapping("/movimentacoes/{id}")
    public MovimentacaoCaixaResponse editar(@PathVariable UUID id,
                                             @Valid @RequestBody LancamentoManualCaixaRequest request) {
        return paraResponse(editarMovimentacaoManualCaixaUseCase.editar(id, request.paraComando()));
    }

    @PatchMapping("/movimentacoes/{id}/realizar")
    public MovimentacaoCaixaResponse realizar(@PathVariable UUID id,
                                               @RequestBody(required = false) RealizarMovimentacaoCaixaRequest request) {
        LocalDate dataEfetiva = request == null ? null : request.dataEfetiva();
        return paraResponse(realizarMovimentacaoManualCaixaUseCase.realizar(id, dataEfetiva));
    }

    @PatchMapping("/movimentacoes/{id}/cancelar")
    public MovimentacaoCaixaResponse cancelar(@PathVariable UUID id,
                                               @Valid @RequestBody CancelarMovimentacaoCaixaRequest request) {
        return paraResponse(cancelarMovimentacaoManualCaixaUseCase.cancelar(id, request.motivo()));
    }

    @GetMapping("/resumo")
    public ResumoCaixaResponse resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResumoCaixaResponse.de(consultarFluxoCaixaUseCase.consultarResumo(inicio, fim));
    }

    @GetMapping("/saldo-acumulado")
    public ResumoCaixaResponse saldoAcumulado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return ResumoCaixaResponse.de(consultarFluxoCaixaUseCase.consultarSaldoAcumulado(ate));
    }

    @GetMapping("/resumo-por-categoria")
    public List<ResumoCategoriaCaixaResponse> resumoPorCategoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return consultarFluxoCaixaUseCase.consultarResumoPorCategoria(inicio, fim).stream()
                .map(item -> ResumoCategoriaCaixaResponse.de(item, buscarCategoria(item.categoriaId())))
                .toList();
    }

    private MovimentacaoCaixaResponse paraResponse(MovimentacaoCaixa movimentacao) {
        return MovimentacaoCaixaResponse.de(movimentacao, buscarCategoria(movimentacao.getCategoriaId()));
    }

    private CategoriaFluxoCaixa buscarCategoria(UUID categoriaId) {
        return categoriaId == null ? null : buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(categoriaId);
    }
}
