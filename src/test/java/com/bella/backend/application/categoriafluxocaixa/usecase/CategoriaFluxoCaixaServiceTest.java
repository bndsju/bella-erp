package com.bella.backend.application.categoriafluxocaixa.usecase;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.in.CadastrarCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.EditarCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.out.CategoriaFluxoCaixaRepositoryPort;
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
class CategoriaFluxoCaixaServiceTest {

    @Mock
    private CategoriaFluxoCaixaRepositoryPort categoriaFluxoCaixaRepositoryPort;

    private CategoriaFluxoCaixaService categoriaFluxoCaixaService;

    @BeforeEach
    void setUp() {
        categoriaFluxoCaixaService = new CategoriaFluxoCaixaService(categoriaFluxoCaixaRepositoryPort);
    }

    @Test
    void cadastraCategoriaFluxoCaixa() {
        when(categoriaFluxoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaFluxoCaixa categoriaFluxoCaixa = categoriaFluxoCaixaService.cadastrar(new CadastrarCategoriaFluxoCaixaUseCase.Comando("Bebidas"));

        assertThat(categoriaFluxoCaixa.getNome()).isEqualTo("Bebidas");
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(categoriaFluxoCaixaRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> categoriaFluxoCaixaService.buscarPorId(id));
    }

    @Test
    void editarAtualizaNome() {
        UUID id = UUID.randomUUID();
        CategoriaFluxoCaixa existente = CategoriaFluxoCaixa.novo("Bebidas");
        when(categoriaFluxoCaixaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(categoriaFluxoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaFluxoCaixa editada = categoriaFluxoCaixaService.editar(id, new EditarCategoriaFluxoCaixaUseCase.Comando("Bebidas Geladas"));

        assertThat(editada.getNome()).isEqualTo("Bebidas Geladas");
    }

    @Test
    void alterarStatusInativaCategoriaFluxoCaixa() {
        UUID id = UUID.randomUUID();
        CategoriaFluxoCaixa existente = CategoriaFluxoCaixa.novo("Bebidas");
        when(categoriaFluxoCaixaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(categoriaFluxoCaixaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaFluxoCaixa resultado = categoriaFluxoCaixaService.alterarStatus(id, StatusCategoriaFluxoCaixa.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusCategoriaFluxoCaixa.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        when(categoriaFluxoCaixaRepositoryPort.listar(StatusCategoriaFluxoCaixa.ATIVO)).thenReturn(List.of());

        List<CategoriaFluxoCaixa> resultado = categoriaFluxoCaixaService.listar(StatusCategoriaFluxoCaixa.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
