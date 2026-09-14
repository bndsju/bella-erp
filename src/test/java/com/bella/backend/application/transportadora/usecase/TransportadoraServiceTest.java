package com.bella.backend.application.transportadora.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;
import com.bella.backend.domain.transportadora.port.in.CadastrarTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.EditarTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.out.TransportadoraRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportadoraServiceTest {

    @Mock
    private TransportadoraRepositoryPort transportadoraRepositoryPort;

    private TransportadoraService transportadoraService;

    @BeforeEach
    void setUp() {
        transportadoraService = new TransportadoraService(transportadoraRepositoryPort);
    }

    @Test
    void cadastraTransportadora() {
        when(transportadoraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transportadora transportadora = transportadoraService.cadastrar(
                new CadastrarTransportadoraUseCase.Comando("Rápido Entregas", "11988887777"));

        assertThat(transportadora.getNome()).isEqualTo("Rápido Entregas");
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(transportadoraRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> transportadoraService.buscarPorId(id));
    }

    @Test
    void editarAtualizaNome() {
        UUID id = UUID.randomUUID();
        Transportadora existente = Transportadora.novo("Rápido Entregas", null);
        when(transportadoraRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(transportadoraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transportadora editada = transportadoraService.editar(
                id, new EditarTransportadoraUseCase.Comando("Rápido Entregas Ltda", "1133334444"));

        assertThat(editada.getNome()).isEqualTo("Rápido Entregas Ltda");
    }

    @Test
    void alterarStatusInativaTransportadora() {
        UUID id = UUID.randomUUID();
        Transportadora existente = Transportadora.novo("Rápido Entregas", null);
        when(transportadoraRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(transportadoraRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transportadora resultado = transportadoraService.alterarStatus(id, StatusTransportadora.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusTransportadora.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        when(transportadoraRepositoryPort.listar(StatusTransportadora.ATIVO)).thenReturn(List.of());

        List<Transportadora> resultado = transportadoraService.listar(StatusTransportadora.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
