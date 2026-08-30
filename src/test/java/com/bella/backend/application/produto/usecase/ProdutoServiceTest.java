package com.bella.backend.application.produto.usecase;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;
import com.bella.backend.domain.produto.port.in.CadastrarProdutoUseCase;
import com.bella.backend.domain.produto.port.in.EditarProdutoUseCase;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase.FiltroListagem;
import com.bella.backend.domain.produto.port.out.ProdutoRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;
import com.bella.backend.domain.unidademedida.port.in.BuscarUnidadeMedidaPorIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepositoryPort produtoRepositoryPort;

    @Mock
    private BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;

    @Mock
    private BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase;

    private ProdutoService produtoService;

    private static final UUID CATEGORIA_ID = UUID.randomUUID();
    private static final UUID UNIDADE_MEDIDA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        produtoService = new ProdutoService(produtoRepositoryPort, buscarCategoriaPorIdUseCase,
                buscarUnidadeMedidaPorIdUseCase);
    }

    private CadastrarProdutoUseCase.Comando comandoCadastroValido() {
        return new CadastrarProdutoUseCase.Comando("SKU-001", "7891234567890", "Arroz Branco", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, new BigDecimal("10.00"), new BigDecimal("15.00"),
                new BigDecimal("5"));
    }

    @Test
    void cadastraProdutoQuandoReferenciasECodigosValidos() {
        when(buscarCategoriaPorIdUseCase.buscarPorId(CATEGORIA_ID)).thenReturn(Categoria.novo("Alimentos"));
        when(buscarUnidadeMedidaPorIdUseCase.buscarPorId(UNIDADE_MEDIDA_ID))
                .thenReturn(UnidadeMedida.novo("Quilograma", "kg"));
        when(produtoRepositoryPort.existeCodigoInterno(eq("SKU-001"), eq(null))).thenReturn(false);
        when(produtoRepositoryPort.existeCodigoBarras(eq("7891234567890"), eq(null))).thenReturn(false);
        when(produtoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Produto produto = produtoService.cadastrar(comandoCadastroValido());

        assertThat(produto.getCodigoInterno()).isEqualTo("SKU-001");
        verify(produtoRepositoryPort).salvar(any());
    }

    @Test
    void rejeitaCadastroComCategoriaInexistente() {
        when(buscarCategoriaPorIdUseCase.buscarPorId(CATEGORIA_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Categoria", CATEGORIA_ID));

        assertThrows(EntidadeNaoEncontradaException.class, () -> produtoService.cadastrar(comandoCadastroValido()));
        verify(produtoRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaCadastroComCodigoInternoDuplicado() {
        when(buscarCategoriaPorIdUseCase.buscarPorId(CATEGORIA_ID)).thenReturn(Categoria.novo("Alimentos"));
        when(buscarUnidadeMedidaPorIdUseCase.buscarPorId(UNIDADE_MEDIDA_ID))
                .thenReturn(UnidadeMedida.novo("Quilograma", "kg"));
        when(produtoRepositoryPort.existeCodigoInterno(eq("SKU-001"), eq(null))).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> produtoService.cadastrar(comandoCadastroValido()));
        verify(produtoRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaCadastroComCodigoBarrasDuplicado() {
        when(buscarCategoriaPorIdUseCase.buscarPorId(CATEGORIA_ID)).thenReturn(Categoria.novo("Alimentos"));
        when(buscarUnidadeMedidaPorIdUseCase.buscarPorId(UNIDADE_MEDIDA_ID))
                .thenReturn(UnidadeMedida.novo("Quilograma", "kg"));
        when(produtoRepositoryPort.existeCodigoInterno(eq("SKU-001"), eq(null))).thenReturn(false);
        when(produtoRepositoryPort.existeCodigoBarras(eq("7891234567890"), eq(null))).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> produtoService.cadastrar(comandoCadastroValido()));
        verify(produtoRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(produtoRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> produtoService.buscarPorId(id));
    }

    @Test
    void editarAtualizaProduto() {
        UUID id = UUID.randomUUID();
        Produto existente = Produto.novo("SKU-001", null, "Arroz Branco", null, CATEGORIA_ID, UNIDADE_MEDIDA_ID,
                new BigDecimal("10.00"), new BigDecimal("15.00"), new BigDecimal("5"));
        when(produtoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(buscarCategoriaPorIdUseCase.buscarPorId(CATEGORIA_ID)).thenReturn(Categoria.novo("Alimentos"));
        when(buscarUnidadeMedidaPorIdUseCase.buscarPorId(UNIDADE_MEDIDA_ID))
                .thenReturn(UnidadeMedida.novo("Quilograma", "kg"));
        when(produtoRepositoryPort.existeCodigoBarras(eq("789"), eq(id))).thenReturn(false);
        when(produtoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Produto editado = produtoService.editar(id, new EditarProdutoUseCase.Comando("789", "Arroz Integral", null,
                CATEGORIA_ID, UNIDADE_MEDIDA_ID, new BigDecimal("12.00"), new BigDecimal("18.00"),
                new BigDecimal("3")));

        assertThat(editado.getNome()).isEqualTo("Arroz Integral");
        assertThat(editado.getCodigoInterno()).isEqualTo("SKU-001");
    }

    @Test
    void alterarStatusInativaProduto() {
        UUID id = UUID.randomUUID();
        Produto existente = Produto.novo("SKU-001", null, "Arroz Branco", null, CATEGORIA_ID, UNIDADE_MEDIDA_ID,
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO);
        when(produtoRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(produtoRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Produto resultado = produtoService.alterarStatus(id, StatusProduto.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusProduto.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        FiltroListagem filtro = new FiltroListagem("Arroz", StatusProduto.ATIVO, null);
        when(produtoRepositoryPort.listar(filtro)).thenReturn(List.of());

        List<Produto> resultado = produtoService.listar(filtro);

        assertThat(resultado).isEmpty();
    }
}
