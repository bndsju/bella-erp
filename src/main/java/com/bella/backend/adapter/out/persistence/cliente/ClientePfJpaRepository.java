package com.bella.backend.adapter.out.persistence.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientePfJpaRepository extends JpaRepository<ClientePfJpaEntity, UUID> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndClienteIdNot(String cpf, UUID clienteId);
}
