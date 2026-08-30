package com.bella.backend.domain.produto.port.out;

import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase.FiltroListagem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepositoryPort {

    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(UUID id);

    List<Produto> listar(FiltroListagem filtro);

    boolean existePorId(UUID id);

    /**
     * @param idExcluir id do produto a ignorar na checagem (usado na edição); pode ser null.
     */
    boolean existeCodigoInterno(String codigoInterno, UUID idExcluir);

    /**
     * @param idExcluir id do produto a ignorar na checagem (usado na edição); pode ser null.
     */
    boolean existeCodigoBarras(String codigoBarras, UUID idExcluir);
}
