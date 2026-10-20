package com.bella.backend.domain.fluxocaixa.model;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Totais do fluxo. O saldo realizado considera apenas movimentações efetivadas; o saldo projetado
 * é o realizado mais as entradas previstas menos as saídas previstas. Movimentações canceladas
 * nunca entram nas somas. Como cada operação é um único registro (previsto OU realizado), o
 * previsto nunca se soma ao realizado da mesma operação.
 */
public record ResumoCaixa(
        BigDecimal entradasPrevistas,
        BigDecimal saidasPrevistas,
        BigDecimal entradasRealizadas,
        BigDecimal saidasRealizadas
) {

    public static ResumoCaixa de(Collection<MovimentacaoCaixa> movimentacoes) {
        BigDecimal entradasPrevistas = BigDecimal.ZERO;
        BigDecimal saidasPrevistas = BigDecimal.ZERO;
        BigDecimal entradasRealizadas = BigDecimal.ZERO;
        BigDecimal saidasRealizadas = BigDecimal.ZERO;

        for (MovimentacaoCaixa movimentacao : movimentacoes) {
            if (movimentacao.isCancelada()) {
                continue;
            }
            boolean entrada = movimentacao.getTipo() == TipoMovimentacaoCaixa.ENTRADA;
            boolean realizada = movimentacao.getSituacao() == SituacaoMovimentacaoCaixa.REALIZADO;
            BigDecimal valor = movimentacao.getValor();

            if (entrada && realizada) {
                entradasRealizadas = entradasRealizadas.add(valor);
            } else if (entrada) {
                entradasPrevistas = entradasPrevistas.add(valor);
            } else if (realizada) {
                saidasRealizadas = saidasRealizadas.add(valor);
            } else {
                saidasPrevistas = saidasPrevistas.add(valor);
            }
        }
        return new ResumoCaixa(entradasPrevistas, saidasPrevistas, entradasRealizadas, saidasRealizadas);
    }

    public BigDecimal saldoRealizado() {
        return entradasRealizadas.subtract(saidasRealizadas);
    }

    public BigDecimal saldoProjetado() {
        return saldoRealizado().add(entradasPrevistas).subtract(saidasPrevistas);
    }
}
