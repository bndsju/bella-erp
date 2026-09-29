package com.bella.backend.adapter.out.persistence.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SaldoEstoqueJpaRepository extends JpaRepository<SaldoEstoqueJpaEntity, UUID> {
}
