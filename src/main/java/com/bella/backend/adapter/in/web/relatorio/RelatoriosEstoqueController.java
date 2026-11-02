package com.bella.backend.adapter.in.web.relatorio;

import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.RelatorioMovimentacoes;
import com.bella.backend.domain.relatorio.model.RelatoriosEstoque.RelatorioPosicao;
import com.bella.backend.domain.relatorio.port.in.ConsultarRelatoriosEstoqueUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/relatorios/estoque")
public class RelatoriosEstoqueController {

    private final ConsultarRelatoriosEstoqueUseCase consultarRelatoriosEstoqueUseCase;

    public RelatoriosEstoqueController(ConsultarRelatoriosEstoqueUseCase consultarRelatoriosEstoqueUseCase) {
        this.consultarRelatoriosEstoqueUseCase = consultarRelatoriosEstoqueUseCase;
    }

    @GetMapping("/movimentacoes")
    public RelatorioMovimentacoes movimentacoes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) TipoMovimentacaoEstoque tipo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosEstoqueUseCase.movimentacoes(PeriodoRelatorio.opcional(inicio, fim), produtoId,
                tipo, pagina, tamanho);
    }

    @GetMapping("/posicao")
    public RelatorioPosicao posicao(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "false") boolean apenasAbaixoDoMinimo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosEstoqueUseCase.posicao(produtoId, categoriaId, apenasAbaixoDoMinimo, pagina,
                tamanho);
    }

    @GetMapping("/abaixo-do-minimo")
    public RelatorioPosicao abaixoDoMinimo(
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosEstoqueUseCase.posicao(null, categoriaId, true, pagina, tamanho);
    }
}
