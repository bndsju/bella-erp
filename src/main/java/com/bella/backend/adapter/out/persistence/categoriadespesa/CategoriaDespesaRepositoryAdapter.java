package com.bella.backend.adapter.out.persistence.categoriadespesa;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.port.out.CategoriaDespesaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CategoriaDespesaRepositoryAdapter implements CategoriaDespesaRepositoryPort {

    private final CategoriaDespesaJpaRepository categoriaDespesaJpaRepository;

    public CategoriaDespesaRepositoryAdapter(CategoriaDespesaJpaRepository categoriaDespesaJpaRepository) {
        this.categoriaDespesaJpaRepository = categoriaDespesaJpaRepository;
    }

    @Override
    public CategoriaDespesa salvar(CategoriaDespesa categoriaDespesa) {
        CategoriaDespesaJpaEntity salva = categoriaDespesaJpaRepository.save(paraEntidade(categoriaDespesa));
        return paraDominio(salva);
    }

    @Override
    public Optional<CategoriaDespesa> buscarPorId(UUID id) {
        return categoriaDespesaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<CategoriaDespesa> listar(StatusCategoriaDespesa status) {
        List<CategoriaDespesaJpaEntity> entidades = status == null
                ? categoriaDespesaJpaRepository.findAllByOrderByNome()
                : categoriaDespesaJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return categoriaDespesaJpaRepository.existsById(id);
    }

    private CategoriaDespesaJpaEntity paraEntidade(CategoriaDespesa categoriaDespesa) {
        return new CategoriaDespesaJpaEntity(categoriaDespesa.getId(), categoriaDespesa.getNome(), categoriaDespesa.getStatus(),
                categoriaDespesa.getCriadoEm(), categoriaDespesa.getAtualizadoEm());
    }

    private CategoriaDespesa paraDominio(CategoriaDespesaJpaEntity entidade) {
        return CategoriaDespesa.existente(entidade.getId(), entidade.getNome(), entidade.getStatus(),
                entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
