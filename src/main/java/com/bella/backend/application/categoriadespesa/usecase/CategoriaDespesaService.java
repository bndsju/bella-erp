package com.bella.backend.application.categoriadespesa.usecase;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.port.in.AlterarStatusCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.BuscarCategoriaDespesaPorIdUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.CadastrarCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.EditarCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.ListarCategoriaDespesasUseCase;
import com.bella.backend.domain.categoriadespesa.port.out.CategoriaDespesaRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaDespesaService implements
        CadastrarCategoriaDespesaUseCase,
        EditarCategoriaDespesaUseCase,
        BuscarCategoriaDespesaPorIdUseCase,
        ListarCategoriaDespesasUseCase,
        AlterarStatusCategoriaDespesaUseCase {

    private final CategoriaDespesaRepositoryPort categoriaDespesaRepositoryPort;

    public CategoriaDespesaService(CategoriaDespesaRepositoryPort categoriaDespesaRepositoryPort) {
        this.categoriaDespesaRepositoryPort = categoriaDespesaRepositoryPort;
    }

    @Override
    public CategoriaDespesa cadastrar(CadastrarCategoriaDespesaUseCase.Comando comando) {
        CategoriaDespesa categoriaDespesa = CategoriaDespesa.novo(comando.nome());
        return categoriaDespesaRepositoryPort.salvar(categoriaDespesa);
    }

    @Override
    public CategoriaDespesa editar(UUID id, EditarCategoriaDespesaUseCase.Comando comando) {
        CategoriaDespesa categoriaDespesa = buscarPorId(id);
        categoriaDespesa.editar(comando.nome());
        return categoriaDespesaRepositoryPort.salvar(categoriaDespesa);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDespesa buscarPorId(UUID id) {
        return categoriaDespesaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("CategoriaDespesa", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDespesa> listar(StatusCategoriaDespesa status) {
        return categoriaDespesaRepositoryPort.listar(status);
    }

    @Override
    public CategoriaDespesa alterarStatus(UUID id, StatusCategoriaDespesa novoStatus) {
        CategoriaDespesa categoriaDespesa = buscarPorId(id);
        if (novoStatus == StatusCategoriaDespesa.ATIVO) {
            categoriaDespesa.ativar();
        } else {
            categoriaDespesa.inativar();
        }
        return categoriaDespesaRepositoryPort.salvar(categoriaDespesa);
    }
}
