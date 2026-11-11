package com.bella.backend.adapter.out.persistence.usuario;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    Optional<UsuarioJpaEntity> findByLogin(String login);

    boolean existsByLogin(String login);

    List<UsuarioJpaEntity> findAllByOrderByNome();

    List<UsuarioJpaEntity> findByStatusOrderByNome(StatusUsuario status);

    long countByPerfilAndStatus(PerfilUsuario perfil, StatusUsuario status);
}
