package com.bella.backend.application.fornecedor.usecase;

import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.CadastrarFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.EditarFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.out.FornecedorRepositoryPort;
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
class FornecedorServiceTest {

    @Mock
    private FornecedorRepositoryPort fornecedorRepositoryPort;

    private FornecedorService fornecedorService;

    @BeforeEach
    void setUp() {
        fornecedorService = new FornecedorService(fornecedorRepositoryPort);
    }

    @Test
    void cadastraFornecedor() {
        when(fornecedorRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Fornecedor fornecedor = fornecedorService.cadastrar(
                new CadastrarFornecedorUseCase.Comando("Rápido Entregas", "11988887777"));

        assertThat(fornecedor.getNome()).isEqualTo("Rápido Entregas");
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(fornecedorRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> fornecedorService.buscarPorId(id));
    }

    @Test
    void editarAtualizaNome() {
        UUID id = UUID.randomUUID();
        Fornecedor existente = Fornecedor.novo("Rápido Entregas", null);
        when(fornecedorRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(fornecedorRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Fornecedor editada = fornecedorService.editar(
                id, new EditarFornecedorUseCase.Comando("Rápido Entregas Ltda", "1133334444"));

        assertThat(editada.getNome()).isEqualTo("Rápido Entregas Ltda");
    }

    @Test
    void alterarStatusInativaFornecedor() {
        UUID id = UUID.randomUUID();
        Fornecedor existente = Fornecedor.novo("Rápido Entregas", null);
        when(fornecedorRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(fornecedorRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Fornecedor resultado = fornecedorService.alterarStatus(id, StatusFornecedor.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusFornecedor.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        when(fornecedorRepositoryPort.listar(StatusFornecedor.ATIVO)).thenReturn(List.of());

        List<Fornecedor> resultado = fornecedorService.listar(StatusFornecedor.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
