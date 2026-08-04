package com.bella.backend.domain.cliente.port.out;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.port.in.ListarClientesUseCase.FiltroListagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepositoryPort {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    List<Cliente> listar(FiltroListagem filtro);

    boolean existePorId(UUID id);

    /**
     * @param idExcluir id do cliente a ignorar na checagem (usado na edição, para não conflitar consigo mesmo); pode ser null.
     */
    boolean existeCpf(String cpf, UUID idExcluir);

    /**
     * @param idExcluir id do cliente a ignorar na checagem (usado na edição, para não conflitar consigo mesmo); pode ser null.
     */
    boolean existeCnpj(String cnpj, UUID idExcluir);
}
