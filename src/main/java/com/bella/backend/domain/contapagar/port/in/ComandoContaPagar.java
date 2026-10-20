package com.bella.backend.domain.contapagar.port.in;

import com.bella.backend.domain.contapagar.model.OrigemContaPagar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comando compartilhado entre cadastro e edição completa. Pagamento único: informe apenas
 * vencimento. Parcelado: informe apenas parcelas (a soma deve igualar o valor total).
 * origem e origemReferenciaId existem para a integração futura com Compras; a API manual
 * sempre envia MANUAL e sem referência.
 */
public record ComandoContaPagar(
        String descricao,
        UUID fornecedorId,
        UUID categoriaDespesaId,
        BigDecimal valorTotal,
        LocalDate vencimento,
        List<ComandoParcelaContaPagar> parcelas,
        String observacoes,
        OrigemContaPagar origem,
        UUID origemReferenciaId
) {
}
