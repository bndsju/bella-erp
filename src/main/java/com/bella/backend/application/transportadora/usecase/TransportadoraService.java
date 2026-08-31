package com.bella.backend.application.transportadora.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;
import com.bella.backend.domain.transportadora.port.in.AlterarStatusTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.BuscarTransportadoraPorIdUseCase;
import com.bella.backend.domain.transportadora.port.in.CadastrarTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.EditarTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.ListarTransportadorasUseCase;
import com.bella.backend.domain.transportadora.port.out.TransportadoraRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransportadoraService implements
        CadastrarTransportadoraUseCase,
        EditarTransportadoraUseCase,
        BuscarTransportadoraPorIdUseCase,
        ListarTransportadorasUseCase,
        AlterarStatusTransportadoraUseCase {

    private final TransportadoraRepositoryPort transportadoraRepositoryPort;

    public TransportadoraService(TransportadoraRepositoryPort transportadoraRepositoryPort) {
        this.transportadoraRepositoryPort = transportadoraRepositoryPort;
    }

    @Override
    public Transportadora cadastrar(CadastrarTransportadoraUseCase.Comando comando) {
        Transportadora transportadora = Transportadora.novo(comando.nome(), comando.telefone());
        return transportadoraRepositoryPort.salvar(transportadora);
    }

    @Override
    public Transportadora editar(UUID id, EditarTransportadoraUseCase.Comando comando) {
        Transportadora transportadora = buscarPorId(id);
        transportadora.editar(comando.nome(), comando.telefone());
        return transportadoraRepositoryPort.salvar(transportadora);
    }

    @Override
    @Transactional(readOnly = true)
    public Transportadora buscarPorId(UUID id) {
        return transportadoraRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Transportadora", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transportadora> listar(StatusTransportadora status) {
        return transportadoraRepositoryPort.listar(status);
    }

    @Override
    public Transportadora alterarStatus(UUID id, StatusTransportadora novoStatus) {
        Transportadora transportadora = buscarPorId(id);
        if (novoStatus == StatusTransportadora.ATIVO) {
            transportadora.ativar();
        } else {
            transportadora.inativar();
        }
        return transportadoraRepositoryPort.salvar(transportadora);
    }
}
