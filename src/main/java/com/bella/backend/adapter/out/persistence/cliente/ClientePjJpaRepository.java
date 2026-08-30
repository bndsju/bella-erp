package com.bella.backend.adapter.out.persistence.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientePjJpaRepository extends JpaRepository<ClientePjJpaEntity, UUID> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndClienteIdNot(String cnpj, UUID clienteId);
}
