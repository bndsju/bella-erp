package com.bella.backend.domain.contareceber.port.in;

import com.bella.backend.domain.contareceber.model.OrigemContaReceber;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Comando compartilhado entre cadastro e edição completa. Recebimento único: informe apenas
 * vencimento. Parcelado: informe apenas parcelas (a soma deve igualar o valor total).
 * origem e origemReferenciaId existem para a integração futura com Vendas; a API manual
 * sempre envia MANUAL e sem referência.
 */
public record ComandoContaReceber(
        String descricao,
        UUID clienteId,
        BigDecimal valorTotal,
        LocalDate vencimento,
        List<ComandoParcelaContaReceber> parcelas,
        String observacoes,
        OrigemContaReceber origem,
        UUID origemReferenciaId
) {
}
