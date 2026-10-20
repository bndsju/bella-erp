package com.bella.backend.application.categoriafluxocaixa.usecase;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.in.AlterarStatusCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.BuscarCategoriaFluxoCaixaPorIdUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.CadastrarCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.EditarCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.ListarCategoriaFluxoCaixasUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.out.CategoriaFluxoCaixaRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaFluxoCaixaService implements
        CadastrarCategoriaFluxoCaixaUseCase,
        EditarCategoriaFluxoCaixaUseCase,
        BuscarCategoriaFluxoCaixaPorIdUseCase,
        ListarCategoriaFluxoCaixasUseCase,
        AlterarStatusCategoriaFluxoCaixaUseCase {

    private final CategoriaFluxoCaixaRepositoryPort categoriaFluxoCaixaRepositoryPort;

    public CategoriaFluxoCaixaService(CategoriaFluxoCaixaRepositoryPort categoriaFluxoCaixaRepositoryPort) {
        this.categoriaFluxoCaixaRepositoryPort = categoriaFluxoCaixaRepositoryPort;
    }

    @Override
    public CategoriaFluxoCaixa cadastrar(CadastrarCategoriaFluxoCaixaUseCase.Comando comando) {
        CategoriaFluxoCaixa categoriaFluxoCaixa = CategoriaFluxoCaixa.novo(comando.nome());
        return categoriaFluxoCaixaRepositoryPort.salvar(categoriaFluxoCaixa);
    }

    @Override
    public CategoriaFluxoCaixa editar(UUID id, EditarCategoriaFluxoCaixaUseCase.Comando comando) {
        CategoriaFluxoCaixa categoriaFluxoCaixa = buscarPorId(id);
        categoriaFluxoCaixa.editar(comando.nome());
        return categoriaFluxoCaixaRepositoryPort.salvar(categoriaFluxoCaixa);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaFluxoCaixa buscarPorId(UUID id) {
        return categoriaFluxoCaixaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("CategoriaFluxoCaixa", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaFluxoCaixa> listar(StatusCategoriaFluxoCaixa status) {
        return categoriaFluxoCaixaRepositoryPort.listar(status);
    }

    @Override
    public CategoriaFluxoCaixa alterarStatus(UUID id, StatusCategoriaFluxoCaixa novoStatus) {
        CategoriaFluxoCaixa categoriaFluxoCaixa = buscarPorId(id);
        if (novoStatus == StatusCategoriaFluxoCaixa.ATIVO) {
            categoriaFluxoCaixa.ativar();
        } else {
            categoriaFluxoCaixa.inativar();
        }
        return categoriaFluxoCaixaRepositoryPort.salvar(categoriaFluxoCaixa);
    }
}
