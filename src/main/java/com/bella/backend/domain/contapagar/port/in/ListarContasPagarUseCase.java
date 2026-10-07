package com.bella.backend.domain.contapagar.port.in;

import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListarContasPagarUseCase {

    List<ContaPagar> listar(FiltroListagem filtro);

    /**
     * O período filtra pelo vencimento das parcelas (limites inclusivos): a conta entra se ao menos
     * uma parcela vence dentro dele. situacao recorta por em aberto, pagas ou vencidas (hoje).
     */
    record FiltroListagem(UUID fornecedorId, UUID categoriaDespesaId, StatusContaPagar status,
                           SituacaoContaPagar situacao, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
    }
}
