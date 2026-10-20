package com.bella.backend.adapter.out.persistence.categoriafluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaFluxoCaixaJpaRepository extends JpaRepository<CategoriaFluxoCaixaJpaEntity, UUID> {

    List<CategoriaFluxoCaixaJpaEntity> findByStatusOrderByNome(StatusCategoriaFluxoCaixa status);

    List<CategoriaFluxoCaixaJpaEntity> findAllByOrderByNome();
}
