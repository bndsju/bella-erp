package com.bella.backend.adapter.in.web.produto;

import com.bella.backend.adapter.in.web.categoria.CategoriaResponse;
import com.bella.backend.adapter.in.web.unidademedida.UnidadeMedidaResponse;
import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.model.StatusProduto;
import com.bella.backend.domain.produto.port.in.AlterarStatusProdutoUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.produto.port.in.CadastrarProdutoUseCase;
import com.bella.backend.domain.produto.port.in.EditarProdutoUseCase;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase;
import com.bella.backend.domain.produto.port.in.ListarProdutosUseCase.FiltroListagem;
import com.bella.backend.domain.unidademedida.port.in.BuscarUnidadeMedidaPorIdUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final CadastrarProdutoUseCase cadastrarProdutoUseCase;
    private final EditarProdutoUseCase editarProdutoUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final ListarProdutosUseCase listarProdutosUseCase;
    private final AlterarStatusProdutoUseCase alterarStatusProdutoUseCase;
    private final BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;
    private final BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase;

    public ProdutoController(
            CadastrarProdutoUseCase cadastrarProdutoUseCase,
            EditarProdutoUseCase editarProdutoUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
            ListarProdutosUseCase listarProdutosUseCase,
            AlterarStatusProdutoUseCase alterarStatusProdutoUseCase,
            BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase,
            BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase
    ) {
        this.cadastrarProdutoUseCase = cadastrarProdutoUseCase;
        this.editarProdutoUseCase = editarProdutoUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.listarProdutosUseCase = listarProdutosUseCase;
        this.alterarStatusProdutoUseCase = alterarStatusProdutoUseCase;
        this.buscarCategoriaPorIdUseCase = buscarCategoriaPorIdUseCase;
        this.buscarUnidadeMedidaPorIdUseCase = buscarUnidadeMedidaPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        Produto produto = cadastrarProdutoUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/produtos/" + produto.getId())).body(paraResponse(produto));
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarProdutoPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) String busca,
                                         @RequestParam(required = false) StatusProduto status,
                                         @RequestParam(required = false) UUID categoriaId) {
        return listarProdutosUseCase.listar(new FiltroListagem(busca, status, categoriaId)).stream()
                .map(this::paraResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public ProdutoResponse editar(@PathVariable UUID id, @Valid @RequestBody ProdutoEdicaoRequest request) {
        Produto produto = editarProdutoUseCase.editar(id, request.paraComando());
        return paraResponse(produto);
    }

    @PatchMapping("/{id}/status")
    public ProdutoResponse alterarStatus(@PathVariable UUID id,
                                          @Valid @RequestBody AlterarStatusProdutoRequest request) {
        Produto produto = alterarStatusProdutoUseCase.alterarStatus(id, request.status());
        return paraResponse(produto);
    }

    private ProdutoResponse paraResponse(Produto produto) {
        CategoriaResponse categoria = CategoriaResponse.de(
                buscarCategoriaPorIdUseCase.buscarPorId(produto.getCategoriaId()));
        UnidadeMedidaResponse unidadeMedida = UnidadeMedidaResponse.de(
                buscarUnidadeMedidaPorIdUseCase.buscarPorId(produto.getUnidadeMedidaId()));
        return ProdutoResponse.de(produto, categoria, unidadeMedida);
    }
}
