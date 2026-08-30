package com.bella.backend.application.unidademedida.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;
import com.bella.backend.domain.unidademedida.port.in.CadastrarUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.EditarUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.out.UnidadeMedidaRepositoryPort;
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
class UnidadeMedidaServiceTest {

    @Mock
    private UnidadeMedidaRepositoryPort unidadeMedidaRepositoryPort;

    private UnidadeMedidaService unidadeMedidaService;

    @BeforeEach
    void setUp() {
        unidadeMedidaService = new UnidadeMedidaService(unidadeMedidaRepositoryPort);
    }

    @Test
    void cadastraUnidadeMedida() {
        when(unidadeMedidaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UnidadeMedida unidadeMedida = unidadeMedidaService.cadastrar(
                new CadastrarUnidadeMedidaUseCase.Comando("Quilograma", "kg"));

        assertThat(unidadeMedida.getSigla()).isEqualTo("kg");
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(unidadeMedidaRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> unidadeMedidaService.buscarPorId(id));
    }

    @Test
    void editarAtualizaNomeESigla() {
        UUID id = UUID.randomUUID();
        UnidadeMedida existente = UnidadeMedida.novo("Quilograma", "kg");
        when(unidadeMedidaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(unidadeMedidaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UnidadeMedida editada = unidadeMedidaService.editar(id, new EditarUnidadeMedidaUseCase.Comando("Grama", "g"));

        assertThat(editada.getSigla()).isEqualTo("g");
    }

    @Test
    void alterarStatusInativaUnidadeMedida() {
        UUID id = UUID.randomUUID();
        UnidadeMedida existente = UnidadeMedida.novo("Quilograma", "kg");
        when(unidadeMedidaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(unidadeMedidaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UnidadeMedida resultado = unidadeMedidaService.alterarStatus(id, StatusUnidadeMedida.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusUnidadeMedida.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        when(unidadeMedidaRepositoryPort.listar(StatusUnidadeMedida.ATIVO)).thenReturn(List.of());

        List<UnidadeMedida> resultado = unidadeMedidaService.listar(StatusUnidadeMedida.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
