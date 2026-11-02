package com.bella.backend.adapter.in.web.relatorio;

import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.RelatorioContas;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.RelatorioFluxoCaixa;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.RelatorioVencimentos;
import com.bella.backend.domain.relatorio.model.RelatoriosFinanceiros.TipoTitulo;
import com.bella.backend.domain.relatorio.port.in.ConsultarRelatoriosFinanceirosUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Relatórios financeiros. Em contas e vencimentos o período filtra o vencimento e é opcional (sem
 * período, considera tudo); no fluxo de caixa é um período de atividade (padrão: mês corrente).
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatoriosFinanceirosController {

    private final ConsultarRelatoriosFinanceirosUseCase consultarRelatoriosFinanceirosUseCase;

    public RelatoriosFinanceirosController(
            ConsultarRelatoriosFinanceirosUseCase consultarRelatoriosFinanceirosUseCase) {
        this.consultarRelatoriosFinanceirosUseCase = consultarRelatoriosFinanceirosUseCase;
    }

    @GetMapping("/contas-receber")
    public RelatorioContas contasReceber(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusContaReceber status,
            @RequestParam(required = false) SituacaoContaReceber situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosFinanceirosUseCase.contasReceber(PeriodoRelatorio.opcional(inicio, fim),
                clienteId, status, situacao, pagina, tamanho);
    }

    @GetMapping("/contas-pagar")
    public RelatorioContas contasPagar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) UUID categoriaDespesaId,
            @RequestParam(required = false) StatusContaPagar status,
            @RequestParam(required = false) SituacaoContaPagar situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosFinanceirosUseCase.contasPagar(PeriodoRelatorio.opcional(inicio, fim),
                fornecedorId, categoriaDespesaId, status, situacao, pagina, tamanho);
    }

    @GetMapping("/vencimentos")
    public RelatorioVencimentos vencimentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) TipoTitulo tipo,
            @RequestParam(defaultValue = "false") boolean apenasVencidos,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosFinanceirosUseCase.vencimentos(PeriodoRelatorio.opcional(inicio, fim), tipo,
                apenasVencidos, pagina, tamanho);
    }

    @GetMapping("/fluxo-caixa")
    public RelatorioFluxoCaixa fluxoCaixa(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) TipoMovimentacaoCaixa tipo,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(required = false) OrigemMovimentacaoCaixa origem,
            @RequestParam(required = false) SituacaoMovimentacaoCaixa situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosFinanceirosUseCase.fluxoCaixa(PeriodoRelatorio.opcional(inicio, fim), tipo,
                categoriaId, origem, situacao, pagina, tamanho);
    }
}
