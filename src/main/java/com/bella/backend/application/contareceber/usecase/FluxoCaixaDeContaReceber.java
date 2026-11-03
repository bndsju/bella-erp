package com.bella.backend.application.contareceber.usecase;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.RecebimentoContaReceber;
import com.bella.backend.domain.contareceber.model.ParcelaContaReceber;
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
 * Mantém o fluxo de caixa em sincronia com uma conta a receber: cada parcela é projetada como entrada
 * prevista; cada recebimento vira uma entrada realizada (valor recebido + juros + multa, o que de fato
 * entra no caixa) e a projeção da parcela é trocada pelo saldo restante.
 */
@Component
class FluxoCaixaDeContaReceber {

    /** Tamanho da coluna de descrição da movimentação de caixa. */
    private static final int LIMITE_DESCRICAO = 200;
    private static final OrigemMovimentacaoCaixa ORIGEM = OrigemMovimentacaoCaixa.CONTA_RECEBER;

    private final RegistrarMovimentacaoAutomaticaCaixaUseCase registrar;
    private final CancelarMovimentacaoAutomaticaCaixaUseCase cancelar;
    private final ConsultarMovimentacaoAutomaticaCaixaUseCase consultar;

    FluxoCaixaDeContaReceber(RegistrarMovimentacaoAutomaticaCaixaUseCase registrar,
                           CancelarMovimentacaoAutomaticaCaixaUseCase cancelar,
                           ConsultarMovimentacaoAutomaticaCaixaUseCase consultar) {
        this.registrar = registrar;
        this.cancelar = cancelar;
        this.consultar = consultar;
    }

    void projetar(ContaReceber conta) {
        for (ParcelaContaReceber parcela : conta.getParcelas()) {
            projetarParcela(conta, parcela, 0, parcela.getValor());
        }
    }

    /** Chamar antes de editar a conta: só é possível editar valor/parcelas sem recebimentos. */
    void cancelarProjecoes(List<ParcelaContaReceber> parcelas, String motivo) {
        for (ParcelaContaReceber parcela : parcelas) {
            cancelarProjecaoVigente(parcela.getId(), 0, motivo);
        }
    }

    void aposRecebimento(ContaReceber conta, ParcelaContaReceber parcela, RecebimentoContaReceber recebimento,
                       int recebimentosAnteriores) {
        cancelarProjecaoVigente(parcela.getId(), recebimentosAnteriores, "Substituída pelo recebimento realizado");

        if (recebimento.getValorTotalRecebido().signum() > 0) {
            registrar.registrar(new ComandoMovimentacaoAutomaticaCaixa(
                    TipoMovimentacaoCaixa.ENTRADA,
                    descricao(conta, parcela),
                    null,
                    recebimento.getValorTotalRecebido(),
                    SituacaoMovimentacaoCaixa.REALIZADO,
                    parcela.getVencimento(),
                    recebimento.getDataRecebimento(),
                    ORIGEM,
                    recebimento.getId(),
                    recebimento.getObservacao()));
        }
        if (parcela.getSaldoPendente().signum() > 0) {
            projetarParcela(conta, parcela, recebimentosAnteriores + 1, parcela.getSaldoPendente());
        }
    }

    private void projetarParcela(ContaReceber conta, ParcelaContaReceber parcela, int recebimentos,
                                 java.math.BigDecimal valor) {
        registrar.registrar(new ComandoMovimentacaoAutomaticaCaixa(
                TipoMovimentacaoCaixa.ENTRADA,
                descricao(conta, parcela),
                null,
                valor,
                SituacaoMovimentacaoCaixa.PREVISTO,
                parcela.getVencimento(),
                null,
                ORIGEM,
                ReferenciaProjecaoParcela.de(parcela.getId(), recebimentos),
                null));
    }

    /** Contas anteriores à integração não têm projeção: nesse caso não há o que cancelar. */
    private void cancelarProjecaoVigente(UUID parcelaId, int recebimentos, String motivo) {
        UUID referencia = ReferenciaProjecaoParcela.de(parcelaId, recebimentos);
        consultar.consultarPorOrigem(ORIGEM, referencia)
                .filter(movimentacao -> !movimentacao.isCancelada())
                .map(MovimentacaoCaixa::getOrigemReferenciaId)
                .ifPresent(ref -> cancelar.cancelar(ORIGEM, ref, motivo));
    }

    private String descricao(ContaReceber conta, ParcelaContaReceber parcela) {
        String sufixo = " (parcela " + parcela.getNumero() + "/" + conta.getParcelas().size() + ")";
        int espaco = LIMITE_DESCRICAO - "Conta a receber: ".length() - sufixo.length();
        String descricao = conta.getDescricao().length() > espaco
                ? conta.getDescricao().substring(0, espaco)
                : conta.getDescricao();
        return "Conta a receber: " + descricao + sufixo;
    }
}
