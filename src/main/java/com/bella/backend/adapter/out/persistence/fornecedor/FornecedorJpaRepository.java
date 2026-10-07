package com.bella.backend.adapter.out.persistence.fornecedor;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FornecedorJpaRepository extends JpaRepository<FornecedorJpaEntity, UUID> {

    List<FornecedorJpaEntity> findByStatusOrderByNome(StatusFornecedor status);

    List<FornecedorJpaEntity> findAllByOrderByNome();
}
