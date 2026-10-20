package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.cliente.model.Cliente;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContaReceberResponse(
        UUID id,
        String descricao,
        UUID clienteId,
        String clienteNome,
        BigDecimal valorTotal,
        BigDecimal valorRecebido,
        BigDecimal saldoPendente,
        LocalDate dataLancamento,
        String observacoes,
        OrigemContaReceber origem,
        UUID origemReferenciaId,
        StatusContaReceber status,
        boolean vencida,
        List<ParcelaContaReceberResponse> parcelas,
        String motivoCancelamento,
        Instant canceladaEm,
        Instant criadoEm,
        Instant atualizadoEm
) {

    public static ContaReceberResponse de(ContaReceber conta, Cliente cliente, LocalDate hoje) {
        return new ContaReceberResponse(
                conta.getId(),
                conta.getDescricao(),
                cliente == null ? null : cliente.getId(),
                cliente == null ? null : cliente.getNomeExibicao(),
                conta.getValorTotal(),
                conta.getValorRecebido(),
                conta.getSaldoPendente(),
                conta.getDataLancamento(),
                conta.getObservacoes(),
                conta.getOrigem(),
                conta.getOrigemReferenciaId(),
                conta.getStatus(),
                conta.estaVencida(hoje),
                conta.getParcelas().stream().map(parcela -> ParcelaContaReceberResponse.de(parcela, hoje)).toList(),
                conta.getMotivoCancelamento(),
                conta.getCanceladaEm(),
                conta.getCriadoEm(),
                conta.getAtualizadoEm());
    }
}
