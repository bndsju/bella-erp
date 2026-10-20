package com.bella.backend.domain.contareceber.port.in;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListarContasReceberUseCase {

    List<ContaReceber> listar(FiltroListagem filtro);

    /**
     * O período filtra pelo vencimento das parcelas (limites inclusivos): a conta entra se ao menos
     * uma parcela vence dentro dele. situacao recorta por em aberto, recebidas ou vencidas (hoje).
     */
    record FiltroListagem(UUID clienteId, StatusContaReceber status,
                           SituacaoContaReceber situacao, LocalDate vencimentoInicio, LocalDate vencimentoFim) {
    }
}
