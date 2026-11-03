package com.bella.backend.application.contapagar.usecase;

import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.PagamentoContaPagar;
import com.bella.backend.domain.contapagar.model.ParcelaContaPagar;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ReferenciaProjecaoParcela;
import com.bella.backend.domain.fluxocaixa.model.SituacaoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.TipoMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.CancelarMovimentacaoAutomaticaCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ComandoMovimentacaoAutomaticaCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ConsultarMovimentacaoAutomaticaCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.RegistrarMovimentacaoAutomaticaCaixaUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mantém o fluxo de caixa em sincronia com uma conta a pagar: cada parcela é projetada como saída
 * prevista; cada pagamento vira uma saída realizada (valor pago + juros + multa, o que de fato sai do
 * caixa) e a projeção da parcela é trocada pelo saldo restante.
 */
@Component
class FluxoCaixaDeContaPagar {

    /** Tamanho da coluna de descrição da movimentação de caixa. */
    private static final int LIMITE_DESCRICAO = 200;
    private static final OrigemMovimentacaoCaixa ORIGEM = OrigemMovimentacaoCaixa.CONTA_PAGAR;

    private final RegistrarMovimentacaoAutomaticaCaixaUseCase registrar;
    private final CancelarMovimentacaoAutomaticaCaixaUseCase cancelar;
    private final ConsultarMovimentacaoAutomaticaCaixaUseCase consultar;

    FluxoCaixaDeContaPagar(RegistrarMovimentacaoAutomaticaCaixaUseCase registrar,
                           CancelarMovimentacaoAutomaticaCaixaUseCase cancelar,
                           ConsultarMovimentacaoAutomaticaCaixaUseCase consultar) {
        this.registrar = registrar;
        this.cancelar = cancelar;
        this.consultar = consultar;
    }

    void projetar(ContaPagar conta) {
        for (ParcelaContaPagar parcela : conta.getParcelas()) {
            projetarParcela(conta, parcela, 0, parcela.getValor());
        }
    }

    /** Chamar antes de editar a conta: só é possível editar valor/parcelas sem pagamentos. */
    void cancelarProjecoes(List<ParcelaContaPagar> parcelas, String motivo) {
        for (ParcelaContaPagar parcela : parcelas) {
            cancelarProjecaoVigente(parcela.getId(), 0, motivo);
        }
    }

    void aposPagamento(ContaPagar conta, ParcelaContaPagar parcela, PagamentoContaPagar pagamento,
                       int pagamentosAnteriores) {
        cancelarProjecaoVigente(parcela.getId(), pagamentosAnteriores, "Substituída pelo pagamento realizado");

        if (pagamento.getValorDesembolsado().signum() > 0) {
            registrar.registrar(new ComandoMovimentacaoAutomaticaCaixa(
                    TipoMovimentacaoCaixa.SAIDA,
                    descricao(conta, parcela),
                    null,
                    pagamento.getValorDesembolsado(),
                    SituacaoMovimentacaoCaixa.REALIZADO,
                    parcela.getVencimento(),
                    pagamento.getDataPagamento(),
                    ORIGEM,
                    pagamento.getId(),
                    pagamento.getObservacao()));
        }
        if (parcela.getSaldoPendente().signum() > 0) {
            projetarParcela(conta, parcela, pagamentosAnteriores + 1, parcela.getSaldoPendente());
        }
    }

    private void projetarParcela(ContaPagar conta, ParcelaContaPagar parcela, int pagamentos,
                                 java.math.BigDecimal valor) {
        registrar.registrar(new ComandoMovimentacaoAutomaticaCaixa(
                TipoMovimentacaoCaixa.SAIDA,
                descricao(conta, parcela),
                null,
                valor,
                SituacaoMovimentacaoCaixa.PREVISTO,
                parcela.getVencimento(),
                null,
                ORIGEM,
                ReferenciaProjecaoParcela.de(parcela.getId(), pagamentos),
                null));
    }

    /** Contas anteriores à integração não têm projeção: nesse caso não há o que cancelar. */
    private void cancelarProjecaoVigente(UUID parcelaId, int pagamentos, String motivo) {
        UUID referencia = ReferenciaProjecaoParcela.de(parcelaId, pagamentos);
        consultar.consultarPorOrigem(ORIGEM, referencia)
                .filter(movimentacao -> !movimentacao.isCancelada())
                .map(MovimentacaoCaixa::getOrigemReferenciaId)
                .ifPresent(ref -> cancelar.cancelar(ORIGEM, ref, motivo));
    }

    private String descricao(ContaPagar conta, ParcelaContaPagar parcela) {
        String sufixo = " (parcela " + parcela.getNumero() + "/" + conta.getParcelas().size() + ")";
        int espaco = LIMITE_DESCRICAO - "Conta a pagar: ".length() - sufixo.length();
        String descricao = conta.getDescricao().length() > espaco
                ? conta.getDescricao().substring(0, espaco)
                : conta.getDescricao();
        return "Conta a pagar: " + descricao + sufixo;
    }
}
