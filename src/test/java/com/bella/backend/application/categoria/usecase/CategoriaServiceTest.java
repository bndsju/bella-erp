package com.bella.backend.application.categoria.usecase;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;
import com.bella.backend.domain.categoria.port.in.CadastrarCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.EditarCategoriaUseCase;
import com.bella.backend.domain.categoria.port.out.CategoriaRepositoryPort;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepositoryPort categoriaRepositoryPort;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepositoryPort);
    }

    @Test
    void cadastraCategoria() {
        when(categoriaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria categoria = categoriaService.cadastrar(new CadastrarCategoriaUseCase.Comando("Bebidas"));

        assertThat(categoria.getNome()).isEqualTo("Bebidas");
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(categoriaRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> categoriaService.buscarPorId(id));
    }

    @Test
    void editarAtualizaNome() {
        UUID id = UUID.randomUUID();
        Categoria existente = Categoria.novo("Bebidas");
        when(categoriaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(categoriaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria editada = categoriaService.editar(id, new EditarCategoriaUseCase.Comando("Bebidas Geladas"));

        assertThat(editada.getNome()).isEqualTo("Bebidas Geladas");
    }

    @Test
    void alterarStatusInativaCategoria() {
        UUID id = UUID.randomUUID();
        Categoria existente = Categoria.novo("Bebidas");
        when(categoriaRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(categoriaRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resultado = categoriaService.alterarStatus(id, StatusCategoria.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusCategoria.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        when(categoriaRepositoryPort.listar(StatusCategoria.ATIVO)).thenReturn(List.of());

        List<Categoria> resultado = categoriaService.listar(StatusCategoria.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
