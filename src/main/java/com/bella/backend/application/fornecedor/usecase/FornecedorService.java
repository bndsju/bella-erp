package com.bella.backend.application.fornecedor.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.AlterarStatusFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.fornecedor.port.in.CadastrarFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.EditarFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.ListarFornecedoresUseCase;
import com.bella.backend.domain.fornecedor.port.out.FornecedorRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FornecedorService implements
        CadastrarFornecedorUseCase,
        EditarFornecedorUseCase,
        BuscarFornecedorPorIdUseCase,
        ListarFornecedoresUseCase,
        AlterarStatusFornecedorUseCase {

    private final FornecedorRepositoryPort fornecedorRepositoryPort;

    public FornecedorService(FornecedorRepositoryPort fornecedorRepositoryPort) {
        this.fornecedorRepositoryPort = fornecedorRepositoryPort;
    }

    @Override
    public Fornecedor cadastrar(CadastrarFornecedorUseCase.Comando comando) {
        Fornecedor fornecedor = Fornecedor.novo(comando.nome(), comando.telefone());
        return fornecedorRepositoryPort.salvar(fornecedor);
    }

    @Override
    public Fornecedor editar(UUID id, EditarFornecedorUseCase.Comando comando) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.editar(comando.nome(), comando.telefone());
        return fornecedorRepositoryPort.salvar(fornecedor);
    }

    @Override
    @Transactional(readOnly = true)
    public Fornecedor buscarPorId(UUID id) {
        return fornecedorRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Fornecedor", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Fornecedor> listar(StatusFornecedor status) {
        return fornecedorRepositoryPort.listar(status);
    }

    @Override
    public Fornecedor alterarStatus(UUID id, StatusFornecedor novoStatus) {
        Fornecedor fornecedor = buscarPorId(id);
        if (novoStatus == StatusFornecedor.ATIVO) {
            fornecedor.ativar();
        } else {
            fornecedor.inativar();
        }
        return fornecedorRepositoryPort.salvar(fornecedor);
    }
}
