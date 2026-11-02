package com.bella.backend.adapter.out.persistence.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AuditoriaJpaRepository
        extends JpaRepository<AuditoriaJpaEntity, UUID>, JpaSpecificationExecutor<AuditoriaJpaEntity> {
}
