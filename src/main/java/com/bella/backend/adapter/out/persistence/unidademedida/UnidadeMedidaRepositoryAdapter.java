package com.bella.backend.adapter.out.persistence.unidademedida;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;
import com.bella.backend.domain.unidademedida.port.out.UnidadeMedidaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UnidadeMedidaRepositoryAdapter implements UnidadeMedidaRepositoryPort {

    private final UnidadeMedidaJpaRepository unidadeMedidaJpaRepository;

    public UnidadeMedidaRepositoryAdapter(UnidadeMedidaJpaRepository unidadeMedidaJpaRepository) {
        this.unidadeMedidaJpaRepository = unidadeMedidaJpaRepository;
    }

    @Override
    public UnidadeMedida salvar(UnidadeMedida unidadeMedida) {
        UnidadeMedidaJpaEntity salva = unidadeMedidaJpaRepository.save(paraEntidade(unidadeMedida));
        return paraDominio(salva);
    }

    @Override
    public Optional<UnidadeMedida> buscarPorId(UUID id) {
        return unidadeMedidaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<UnidadeMedida> listar(StatusUnidadeMedida status) {
        List<UnidadeMedidaJpaEntity> entidades = status == null
                ? unidadeMedidaJpaRepository.findAllByOrderByNome()
                : unidadeMedidaJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return unidadeMedidaJpaRepository.existsById(id);
    }

    private UnidadeMedidaJpaEntity paraEntidade(UnidadeMedida unidadeMedida) {
        return new UnidadeMedidaJpaEntity(unidadeMedida.getId(), unidadeMedida.getNome(), unidadeMedida.getSigla(),
                unidadeMedida.getStatus(), unidadeMedida.getCriadoEm(), unidadeMedida.getAtualizadoEm());
    }

    private UnidadeMedida paraDominio(UnidadeMedidaJpaEntity entidade) {
        return UnidadeMedida.existente(entidade.getId(), entidade.getNome(), entidade.getSigla(),
                entidade.getStatus(), entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
