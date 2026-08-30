package com.bella.backend.adapter.out.persistence.categoria;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;
import com.bella.backend.domain.categoria.port.out.CategoriaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository categoriaJpaRepository;

    public CategoriaRepositoryAdapter(CategoriaJpaRepository categoriaJpaRepository) {
        this.categoriaJpaRepository = categoriaJpaRepository;
    }

    @Override
    public Categoria salvar(Categoria categoria) {
        CategoriaJpaEntity salva = categoriaJpaRepository.save(paraEntidade(categoria));
        return paraDominio(salva);
    }

    @Override
    public Optional<Categoria> buscarPorId(UUID id) {
        return categoriaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Categoria> listar(StatusCategoria status) {
        List<CategoriaJpaEntity> entidades = status == null
                ? categoriaJpaRepository.findAllByOrderByNome()
                : categoriaJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return categoriaJpaRepository.existsById(id);
    }

    private CategoriaJpaEntity paraEntidade(Categoria categoria) {
        return new CategoriaJpaEntity(categoria.getId(), categoria.getNome(), categoria.getStatus(),
                categoria.getCriadoEm(), categoria.getAtualizadoEm());
    }

    private Categoria paraDominio(CategoriaJpaEntity entidade) {
        return Categoria.existente(entidade.getId(), entidade.getNome(), entidade.getStatus(),
                entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
