package com.bella.backend.adapter.out.persistence.categoriadespesa;

import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaDespesaJpaRepository extends JpaRepository<CategoriaDespesaJpaEntity, UUID> {

    List<CategoriaDespesaJpaEntity> findByStatusOrderByNome(StatusCategoriaDespesa status);

    List<CategoriaDespesaJpaEntity> findAllByOrderByNome();
}
