package com.bella.backend.adapter.out.persistence.categoria;

import com.bella.backend.domain.categoria.model.StatusCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, UUID> {

    List<CategoriaJpaEntity> findByStatusOrderByNome(StatusCategoria status);

    List<CategoriaJpaEntity> findAllByOrderByNome();
}
