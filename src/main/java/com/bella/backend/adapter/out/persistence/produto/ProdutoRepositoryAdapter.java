package com.bella.backend.adapter.out.persistence.produto;

import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase.FiltroListagem;
import com.bella.backend.domain.produto.port.out.ProdutoRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProdutoRepositoryAdapter implements ProdutoRepositoryPort {

    private final ProdutoJpaRepository produtoJpaRepository;

    public ProdutoRepositoryAdapter(ProdutoJpaRepository produtoJpaRepository) {
        this.produtoJpaRepository = produtoJpaRepository;
    }

    @Override
    public Produto salvar(Produto produto) {
        ProdutoJpaEntity salvo = produtoJpaRepository.save(paraEntidade(produto));
        return paraDominio(salvo);
    }

    @Override
    public Optional<Produto> buscarPorId(UUID id) {
        return produtoJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Produto> listar(FiltroListagem filtro) {
        String termo = (filtro.termoBusca() == null || filtro.termoBusca().isBlank()) ? null : filtro.termoBusca();
        return produtoJpaRepository.buscarPorFiltro(termo, filtro.status(), filtro.categoriaId()).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return produtoJpaRepository.existsById(id);
    }

    @Override
    public boolean existeCodigoInterno(String codigoInterno, UUID idExcluir) {
        return idExcluir == null
                ? produtoJpaRepository.existsByCodigoInterno(codigoInterno)
                : produtoJpaRepository.existsByCodigoInternoAndIdNot(codigoInterno, idExcluir);
    }

    @Override
    public boolean existeCodigoBarras(String codigoBarras, UUID idExcluir) {
        return idExcluir == null
                ? produtoJpaRepository.existsByCodigoBarras(codigoBarras)
                : produtoJpaRepository.existsByCodigoBarrasAndIdNot(codigoBarras, idExcluir);
    }

    private ProdutoJpaEntity paraEntidade(Produto produto) {
        return new ProdutoJpaEntity(produto.getId(), produto.getCodigoInterno(), produto.getCodigoBarras(),
                produto.getNome(), produto.getDescricao(), produto.getCategoriaId(), produto.getUnidadeMedidaId(),
                produto.getPrecoCusto(), produto.getPrecoVenda(), produto.getEstoqueMinimo(), produto.getStatus(),
                produto.getCriadoEm(), produto.getAtualizadoEm());
    }

    private Produto paraDominio(ProdutoJpaEntity entidade) {
        return Produto.existente(entidade.getId(), entidade.getCodigoInterno(), entidade.getCodigoBarras(),
                entidade.getNome(), entidade.getDescricao(), entidade.getCategoriaId(),
                entidade.getUnidadeMedidaId(), entidade.getPrecoCusto(), entidade.getPrecoVenda(),
                entidade.getEstoqueMinimo(), entidade.getStatus(), entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }
}
