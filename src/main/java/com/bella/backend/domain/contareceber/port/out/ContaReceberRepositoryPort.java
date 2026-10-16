package com.bella.backend.domain.contareceber.port.out;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaReceberRepositoryPort {

    ContaReceber salvar(ContaReceber conta);

    Optional<ContaReceber> buscarPorId(UUID id);

    /** Filtros simples resolvidos no banco; recortes por data e situação ficam na aplicação/domínio. */
    List<ContaReceber> listar(UUID clienteId, StatusContaReceber status);

    boolean existePorOrigemEReferencia(OrigemContaReceber origem, UUID origemReferenciaId);
}
