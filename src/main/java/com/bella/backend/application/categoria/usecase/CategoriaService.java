package com.bella.backend.application.categoria.usecase;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;
import com.bella.backend.domain.categoria.port.in.AlterarStatusCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.categoria.port.in.CadastrarCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.EditarCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.ListarCategoriasUseCase;
import com.bella.backend.domain.categoria.port.out.CategoriaRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaService implements
        CadastrarCategoriaUseCase,
        EditarCategoriaUseCase,
        BuscarCategoriaPorIdUseCase,
        ListarCategoriasUseCase,
        AlterarStatusCategoriaUseCase {

    private final CategoriaRepositoryPort categoriaRepositoryPort;

    public CategoriaService(CategoriaRepositoryPort categoriaRepositoryPort) {
        this.categoriaRepositoryPort = categoriaRepositoryPort;
    }

    @Override
    public Categoria cadastrar(CadastrarCategoriaUseCase.Comando comando) {
        Categoria categoria = Categoria.novo(comando.nome());
        return categoriaRepositoryPort.salvar(categoria);
    }

    @Override
    public Categoria editar(UUID id, EditarCategoriaUseCase.Comando comando) {
        Categoria categoria = buscarPorId(id);
        categoria.editar(comando.nome());
        return categoriaRepositoryPort.salvar(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorId(UUID id) {
        return categoriaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar(StatusCategoria status) {
        return categoriaRepositoryPort.listar(status);
    }

    @Override
    public Categoria alterarStatus(UUID id, StatusCategoria novoStatus) {
        Categoria categoria = buscarPorId(id);
        if (novoStatus == StatusCategoria.ATIVO) {
            categoria.ativar();
        } else {
            categoria.inativar();
        }
        return categoriaRepositoryPort.salvar(categoria);
    }
}
