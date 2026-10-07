package com.bella.backend.application.categoriadespesa.usecase;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.port.in.CadastrarCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.EditarCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.out.CategoriaDespesaRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
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
class CategoriaDespesaServiceTest {

    @Mock
    private CategoriaDespesaRepositoryPort categoriaDespesaRepositoryPort;

    private CategoriaDespesaService categoriaDespesaService;

    @BeforeEach
    void setUp() {
        categoriaDespesaService = new CategoriaDespesaService(categoriaDespesaRepositoryPort);
    }

    @Test
    void cadastraCategoriaDespesa() {
        when(categoriaDespesaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaDespesa categoriaDespesa = categoriaDespesaService.cadastrar(new CadastrarCategoriaDespesaUseCase.Comando("Bebidas"));

        assertThat(categoriaDespesa.getNome()).isEqualTo("Bebidas");
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(categoriaDespesaRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> categoriaDespesaService.buscarPorId(id));
    }

    @Test
    void editarAtualizaNome() {
        UUID id = UUID.randomUUID();
        CategoriaDespesa existente = CategoriaDespesa.novo("Bebidas");
        when(categoriaDespesaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(categoriaDespesaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaDespesa editada = categoriaDespesaService.editar(id, new EditarCategoriaDespesaUseCase.Comando("Bebidas Geladas"));

        assertThat(editada.getNome()).isEqualTo("Bebidas Geladas");
    }

    @Test
    void alterarStatusInativaCategoriaDespesa() {
        UUID id = UUID.randomUUID();
        CategoriaDespesa existente = CategoriaDespesa.novo("Bebidas");
        when(categoriaDespesaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(categoriaDespesaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaDespesa resultado = categoriaDespesaService.alterarStatus(id, StatusCategoriaDespesa.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusCategoriaDespesa.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        when(categoriaDespesaRepositoryPort.listar(StatusCategoriaDespesa.ATIVO)).thenReturn(List.of());

        List<CategoriaDespesa> resultado = categoriaDespesaService.listar(StatusCategoriaDespesa.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
