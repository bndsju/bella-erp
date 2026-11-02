package com.bella.backend.adapter.in.web.relatorio;

import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioClientesMaiorVolume;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioOrcamentos;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioPedidosPorStatus;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioProdutosMaisVendidos;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioVendas;
import com.bella.backend.domain.relatorio.port.in.ConsultarRelatoriosComerciaisUseCase;
import com.bella.backend.domain.venda.model.FormaPagamento;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/** Relatórios comerciais. Sem período, usam o mês corrente; datas são inclusivas. */
@RestController
@RequestMapping("/api/relatorios")
public class RelatoriosComerciaisController {

    private final ConsultarRelatoriosComerciaisUseCase consultarRelatoriosComerciaisUseCase;

    public RelatoriosComerciaisController(ConsultarRelatoriosComerciaisUseCase consultarRelatoriosComerciaisUseCase) {
        this.consultarRelatoriosComerciaisUseCase = consultarRelatoriosComerciaisUseCase;
    }

    @GetMapping("/vendas")
    public RelatorioVendas vendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) FormaPagamento formaPagamento,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return consultarRelatoriosComerciaisUseCase.vendas(PeriodoRelatorio.opcional(inicio, fim), clienteId,
                produtoId, formaPagamento, pagina, tamanho);
    }

    @GetMapping("/pedidos-por-status")
    public RelatorioPedidosPorStatus pedidosPorStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID clienteId) {
        return consultarRelatoriosComerciaisUseCase.pedidosPorStatus(PeriodoRelatorio.opcional(inicio, fim),
                clienteId);
    }

    @GetMapping("/orcamentos")
    public RelatorioOrcamentos orcamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID clienteId) {
        return consultarRelatoriosComerciaisUseCase.orcamentos(PeriodoRelatorio.opcional(inicio, fim), clienteId);
    }

    @GetMapping("/produtos-mais-vendidos")
    public RelatorioProdutosMaisVendidos produtosMaisVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) UUID categoriaId,
            @RequestParam(defaultValue = "10") int limite) {
        return consultarRelatoriosComerciaisUseCase.produtosMaisVendidos(PeriodoRelatorio.opcional(inicio, fim),
                clienteId, categoriaId, limite);
    }

    @GetMapping("/clientes-maior-volume")
    public RelatorioClientesMaiorVolume clientesMaiorVolume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "10") int limite) {
        return consultarRelatoriosComerciaisUseCase.clientesMaiorVolume(PeriodoRelatorio.opcional(inicio, fim),
                limite);
    }
}
