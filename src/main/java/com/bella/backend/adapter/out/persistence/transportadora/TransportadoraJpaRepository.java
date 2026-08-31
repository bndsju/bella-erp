package com.bella.backend.adapter.out.persistence.transportadora;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransportadoraJpaRepository extends JpaRepository<TransportadoraJpaEntity, UUID> {

    List<TransportadoraJpaEntity> findByStatusOrderByNome(StatusTransportadora status);

    List<TransportadoraJpaEntity> findAllByOrderByNome();
}
