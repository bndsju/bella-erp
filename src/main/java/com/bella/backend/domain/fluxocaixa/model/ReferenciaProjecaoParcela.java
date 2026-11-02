package com.bella.backend.domain.fluxocaixa.model;

import java.util.UUID;

/**
 * Referência da projeção (movimentação prevista) de uma parcela. A projeção inicial usa o id da
 * parcela; a cada pagamento parcial ela é substituída por uma nova, do saldo restante, cuja
 * referência deriva do id da parcela e do número de pagamentos já feitos — assim a projeção
 * vigente é sempre calculável sem guardar estado extra.
 */
public final class ReferenciaProjecaoParcela {

    private ReferenciaProjecaoParcela() {
    }

    public static UUID de(UUID parcelaId, int pagamentosRegistrados) {
        if (pagamentosRegistrados == 0) {
            return parcelaId;
        }
        return UUID.nameUUIDFromBytes((parcelaId + "#" + pagamentosRegistrados).getBytes());
    }
}
