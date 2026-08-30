package com.bella.backend.application.unidademedida.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;
import com.bella.backend.domain.unidademedida.port.in.AlterarStatusUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.BuscarUnidadeMedidaPorIdUseCase;
import com.bella.backend.domain.unidademedida.port.in.CadastrarUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.EditarUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.ListarUnidadesMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.out.UnidadeMedidaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UnidadeMedidaService implements
        CadastrarUnidadeMedidaUseCase,
        EditarUnidadeMedidaUseCase,
        BuscarUnidadeMedidaPorIdUseCase,
        ListarUnidadesMedidaUseCase,
        AlterarStatusUnidadeMedidaUseCase {

    private final UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort;

    public UnidadeMedidaService(UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort) {
        this.unidadeMedidaRepositoryPort = unidadeMedidaRepositoryPort;
    }

    @Override
    public UnidadeMedida cadastrar(CadastrarUnidadeMedidaUseCase.Comando comando) {
        UnidadeMedida unidadeMedida = UnidadeMedida.novo(comando.nome(), comando.sigla());
        return unidadeMedidaRepositoryPort.salvar(unidadeMedida);
    }

    @Override
    public UnidadeMedida editar(UUID id, EditarUnidadeMedidaUseCase.Comando comando) {
        UnidadeMedida unidadeMedida = buscarPorId(id);
        unidadeMedida.editar(comando.nome(), comando.sigla());
        return unidadeMedidaRepositoryPort.salvar(unidadeMedida);
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadeMedida buscarPorId(UUID id) {
        return unidadeMedidaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Unidade de medida", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadeMedida> listar(StatusUnidadeMedida status) {
        return unidadeMedidaRepositoryPort.listar(status);
    }

    @Override
    public UnidadeMedida alterarStatus(UUID id, StatusUnidadeMedida novoStatus) {
        UnidadeMedida unidadeMedida = buscarPorId(id);
        if (novoStatus == StatusUnidadeMedida.ATIVO) {
            unidadeMedida.ativar();
        } else {
            unidadeMedida.inativar();
        }
        return unidadeMedidaRepositoryPort.salvar(unidadeMedida);
    }
}
