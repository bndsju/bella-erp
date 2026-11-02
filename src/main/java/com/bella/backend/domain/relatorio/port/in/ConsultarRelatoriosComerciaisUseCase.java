package com.bella.backend.domain.relatorio.port.in;

import com.bella.backend.domain.relatorio.model.PeriodoRelatorio;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioClientesMaiorVolume;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioOrcamentos;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioPedidosPorStatus;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioProdutosMaisVendidos;
import com.bella.backend.domain.relatorio.model.RelatoriosComerciais.RelatorioVendas;
import com.bella.backend.domain.venda.model.FormaPagamento;

import java.util.UUID;

/** Consultas somente leitura sobre vendas, pedidos e orçamentos. Sem período, usam o mês corrente. */
public interface ConsultarRelatoriosComerciaisUseCase {

    RelatorioVendas vendas(PeriodoRelatorio periodo, UUID clienteId, UUID produtoId, FormaPagamento formaPagamento,
                           int pagina, int tamanho);

    RelatorioPedidosPorStatus pedidosPorStatus(PeriodoRelatorio periodo, UUID clienteId);

    RelatorioOrcamentos orcamentos(PeriodoRelatorio periodo, UUID clienteId);

    RelatorioProdutosMaisVendidos produtosMaisVendidos(PeriodoRelatorio periodo, UUID clienteId, UUID categoriaId,
                                                       int limite);

    RelatorioClientesMaiorVolume clientesMaiorVolume(PeriodoRelatorio periodo, int limite);
}
