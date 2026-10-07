package com.bella.backend.adapter.out.persistence.fornecedor;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.out.FornecedorRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FornecedorRepositoryAdapter implements FornecedorRepositoryPort {

    private final FornecedorJpaRepository fornecedorJpaRepository;

    public FornecedorRepositoryAdapter(FornecedorJpaRepository fornecedorJpaRepository) {
        this.fornecedorJpaRepository = fornecedorJpaRepository;
    }

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        FornecedorJpaEntity salva = fornecedorJpaRepository.save(paraEntidade(fornecedor));
        return paraDominio(salva);
    }

    @Override
    public Optional<Fornecedor> buscarPorId(UUID id) {
        return fornecedorJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Fornecedor> listar(StatusFornecedor status) {
        List<FornecedorJpaEntity> entidades = status == null
                ? fornecedorJpaRepository.findAllByOrderByNome()
                : fornecedorJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return fornecedorJpaRepository.existsById(id);
    }

    private FornecedorJpaEntity paraEntidade(Fornecedor fornecedor) {
        return new FornecedorJpaEntity(fornecedor.getId(), fornecedor.getNome(),
                fornecedor.getTelefone(), fornecedor.getStatus(), fornecedor.getCriadoEm(),
                fornecedor.getAtualizadoEm());
    }

    private Fornecedor paraDominio(FornecedorJpaEntity entidade) {
        return Fornecedor.existente(entidade.getId(), entidade.getNome(), entidade.getTelefone(),
                entidade.getStatus(), entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
