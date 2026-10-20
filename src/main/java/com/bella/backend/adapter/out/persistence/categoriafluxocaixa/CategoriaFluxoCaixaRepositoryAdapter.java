package com.bella.backend.adapter.out.persistence.categoriafluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.out.CategoriaFluxoCaixaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CategoriaFluxoCaixaRepositoryAdapter implements CategoriaFluxoCaixaRepositoryPort {

    private final CategoriaFluxoCaixaJpaRepository categoriaFluxoCaixaJpaRepository;

    public CategoriaFluxoCaixaRepositoryAdapter(CategoriaFluxoCaixaJpaRepository categoriaFluxoCaixaJpaRepository) {
        this.categoriaFluxoCaixaJpaRepository = categoriaFluxoCaixaJpaRepository;
    }

    @Override
    public CategoriaFluxoCaixa salvar(CategoriaFluxoCaixa categoriaFluxoCaixa) {
        CategoriaFluxoCaixaJpaEntity salva = categoriaFluxoCaixaJpaRepository.save(paraEntidade(categoriaFluxoCaixa));
        return paraDominio(salva);
    }

    @Override
    public Optional<CategoriaFluxoCaixa> buscarPorId(UUID id) {
        return categoriaFluxoCaixaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<CategoriaFluxoCaixa> listar(StatusCategoriaFluxoCaixa status) {
        List<CategoriaFluxoCaixaJpaEntity> entidades = status == null
                ? categoriaFluxoCaixaJpaRepository.findAllByOrderByNome()
                : categoriaFluxoCaixaJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return categoriaFluxoCaixaJpaRepository.existsById(id);
    }

    private CategoriaFluxoCaixaJpaEntity paraEntidade(CategoriaFluxoCaixa categoriaFluxoCaixa) {
        return new CategoriaFluxoCaixaJpaEntity(categoriaFluxoCaixa.getId(), categoriaFluxoCaixa.getNome(), categoriaFluxoCaixa.getStatus(),
                categoriaFluxoCaixa.getCriadoEm(), categoriaFluxoCaixa.getAtualizadoEm());
    }

    private CategoriaFluxoCaixa paraDominio(CategoriaFluxoCaixaJpaEntity entidade) {
        return CategoriaFluxoCaixa.existente(entidade.getId(), entidade.getNome(), entidade.getStatus(),
                entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
