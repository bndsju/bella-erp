package com.bella.backend.application.produto.usecase;

import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;
import com.bella.backend.domain.produto.port.in.AlterarStatusProdutoUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.produto.port.in.CadastrarProdutoUseCase;
import com.bella.backend.domain.produto.port.in.EditarProdutoUseCase;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase;
import com.bella.backend.domain.produto.port.out.ProdutoRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.unidademedida.port.in.BuscarUnidadeMedidaPorIdUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProdutoService implements
        CadastrarProdutoUseCase,
        EditarProdutoUseCase,
        BuscarProdutoPorIdUseCase,
        ListarProdutosUseCase,
        AlterarStatusProdutoUseCase {

    private final ProdutoRepositoryPort produtoRepositoryPort;
    private final BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;
    private final BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase;

    public ProdutoService(ProdutoRepositoryPort produtoRepositoryPort,
                           BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase,
                           BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase) {
        this.produtoRepositoryPort = produtoRepositoryPort;
        this.buscarCategoriaPorIdUseCase = buscarCategoriaPorIdUseCase;
        this.buscarUnidadeMedidaPorIdUseCase = buscarUnidadeMedidaPorIdUseCase;
    }

    @Override
    public Produto cadastrar(CadastrarProdutoUseCase.Comando comando) {
        validarReferencias(comando.categoriaId(), comando.unidadeMedidaId());
        validarCodigoInternoUnico(comando.codigoInterno(), null);
        validarCodigoBarrasUnico(comando.codigoBarras(), null);

        Produto produto = Produto.novo(comando.codigoInterno(), comando.codigoBarras(), comando.nome(),
                comando.descricao(), comando.categoriaId(), comando.unidadeMedidaId(), comando.precoCusto(),
                comando.precoVenda(), comando.estoqueMinimo());

        return produtoRepositoryPort.salvar(produto);
    }

    @Override
    public Produto editar(UUID id, EditarProdutoUseCase.Comando comando) {
        Produto produto = buscarPorId(id);

        validarReferencias(comando.categoriaId(), comando.unidadeMedidaId());
        validarCodigoBarrasUnico(comando.codigoBarras(), id);

        produto.editar(comando.codigoBarras(), comando.nome(), comando.descricao(), comando.categoriaId(),
                comando.unidadeMedidaId(), comando.precoCusto(), comando.precoVenda(), comando.estoqueMinimo());

        return produtoRepositoryPort.salvar(produto);
    }

    @Override
    @Transactional(readOnly = true)
    public Produto buscarPorId(UUID id) {
        return produtoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listar(FiltroListagem filtro) {
        return produtoRepositoryPort.listar(filtro);
    }

    @Override
    public Produto alterarStatus(UUID id, StatusProduto novoStatus) {
        Produto produto = buscarPorId(id);
        if (novoStatus == StatusProduto.ATIVO) {
            produto.ativar();
        } else {
            produto.inativar();
        }
        return produtoRepositoryPort.salvar(produto);
    }

    private void validarReferencias(UUID categoriaId, UUID unidadeMedidaId) {
        buscarCategoriaPorIdUseCase.buscarPorId(categoriaId);
        buscarUnidadeMedidaPorIdUseCase.buscarPorId(unidadeMedidaId);
    }

    private void validarCodigoInternoUnico(String codigoInterno, UUID idExcluir) {
        if (produtoRepositoryPort.existeCodigoInterno(codigoInterno, idExcluir)) {
            throw new RegraDeNegocioException("Já existe um produto cadastrado com este código interno");
        }
    }

    private void validarCodigoBarrasUnico(String codigoBarras, UUID idExcluir) {
        if (codigoBarras != null && !codigoBarras.isBlank()
                && produtoRepositoryPort.existeCodigoBarras(codigoBarras, idExcluir)) {
            throw new RegraDeNegocioException("Já existe um produto cadastrado com este código de barras");
        }
    }
}
