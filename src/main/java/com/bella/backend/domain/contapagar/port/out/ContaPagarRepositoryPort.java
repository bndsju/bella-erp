package com.bella.backend.domain.contapagar.port.out;

import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaPagarRepositoryPort {

    ContaPagar salvar(ContaPagar conta);

    Optional<ContaPagar> buscarPorId(UUID id);

    /** Filtros simples resolvidos no banco; recortes por data e situação ficam na aplicação/domínio. */
    List<ContaPagar> listar(UUID fornecedorId, UUID categoriaDespesaId, StatusContaPagar status);

    boolean existePorOrigemEReferencia(OrigemContaPagar origem, UUID origemReferenciaId);
}
