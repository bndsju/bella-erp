package com.bella.backend.domain.relatorio.port.in;

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

import java.util.UUID;

public interface ConsultarRelatoriosFinanceirosUseCase {

    /** O período filtra o vencimento das parcelas; ausente, considera todas as contas. */
    RelatorioContas contasReceber(PeriodoRelatorio vencimento, UUID clienteId, StatusContaReceber status,
                                  SituacaoContaReceber situacao, int pagina, int tamanho);

    RelatorioContas contasPagar(PeriodoRelatorio vencimento, UUID fornecedorId, UUID categoriaDespesaId,
                                StatusContaPagar status, SituacaoContaPagar situacao, int pagina, int tamanho);

    /** Parcelas em aberto de contas a receber e/ou a pagar, vencidas e a vencer, em relação a hoje. */
    RelatorioVencimentos vencimentos(PeriodoRelatorio vencimento, TipoTitulo tipo, boolean apenasVencidos,
                                     int pagina, int tamanho);

    RelatorioFluxoCaixa fluxoCaixa(PeriodoRelatorio periodo, TipoMovimentacaoCaixa tipo, UUID categoriaId,
                                   OrigemMovimentacaoCaixa origem, SituacaoMovimentacaoCaixa situacao, int pagina,
                                   int tamanho);
}
