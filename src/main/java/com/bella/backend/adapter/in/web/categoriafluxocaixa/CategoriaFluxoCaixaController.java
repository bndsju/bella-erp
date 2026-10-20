package com.bella.backend.adapter.in.web.categoriafluxocaixa;

import com.bella.backend.domain.categoriafluxocaixa.model.CategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.in.AlterarStatusCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.BuscarCategoriaFluxoCaixaPorIdUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.CadastrarCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.EditarCategoriaFluxoCaixaUseCase;
import com.bella.backend.domain.categoriafluxocaixa.port.in.ListarCategoriaFluxoCaixasUseCase;
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
@RequestMapping("/api/categorias-fluxo-caixa")
public class CategoriaFluxoCaixaController {

    private final CadastrarCategoriaFluxoCaixaUseCase cadastrarCategoriaFluxoCaixaUseCase;
    private final EditarCategoriaFluxoCaixaUseCase editarCategoriaFluxoCaixaUseCase;
    private final BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase;
    private final ListarCategoriaFluxoCaixasUseCase listarCategoriaFluxoCaixasUseCase;
    private final AlterarStatusCategoriaFluxoCaixaUseCase alterarStatusCategoriaFluxoCaixaUseCase;

    public CategoriaFluxoCaixaController(
            CadastrarCategoriaFluxoCaixaUseCase cadastrarCategoriaFluxoCaixaUseCase,
            EditarCategoriaFluxoCaixaUseCase editarCategoriaFluxoCaixaUseCase,
            BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase,
            ListarCategoriaFluxoCaixasUseCase listarCategoriaFluxoCaixasUseCase,
            AlterarStatusCategoriaFluxoCaixaUseCase alterarStatusCategoriaFluxoCaixaUseCase
    ) {
        this.cadastrarCategoriaFluxoCaixaUseCase = cadastrarCategoriaFluxoCaixaUseCase;
        this.editarCategoriaFluxoCaixaUseCase = editarCategoriaFluxoCaixaUseCase;
        this.buscarCategoriaFluxoCaixaPorIdUseCase = buscarCategoriaFluxoCaixaPorIdUseCase;
        this.listarCategoriaFluxoCaixasUseCase = listarCategoriaFluxoCaixasUseCase;
        this.alterarStatusCategoriaFluxoCaixaUseCase = alterarStatusCategoriaFluxoCaixaUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoriaFluxoCaixaResponse> cadastrar(@Valid @RequestBody CategoriaFluxoCaixaRequest request) {
        CategoriaFluxoCaixa categoriaFluxoCaixa = cadastrarCategoriaFluxoCaixaUseCase.cadastrar(new CadastrarCategoriaFluxoCaixaUseCase.Comando(request.nome()));
        return ResponseEntity.created(URI.create("/api/categorias-fluxo-caixa/" + categoriaFluxoCaixa.getId()))
                .body(CategoriaFluxoCaixaResponse.de(categoriaFluxoCaixa));
    }

    @GetMapping("/{id}")
    public CategoriaFluxoCaixaResponse buscarPorId(@PathVariable UUID id) {
        return CategoriaFluxoCaixaResponse.de(buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<CategoriaFluxoCaixaResponse> listar(@RequestParam(required = false) StatusCategoriaFluxoCaixa status) {
        return listarCategoriaFluxoCaixasUseCase.listar(status).stream().map(CategoriaFluxoCaixaResponse::de).toList();
    }

    @PutMapping("/{id}")
    public CategoriaFluxoCaixaResponse editar(@PathVariable UUID id, @Valid @RequestBody CategoriaFluxoCaixaRequest request) {
        CategoriaFluxoCaixa categoriaFluxoCaixa = editarCategoriaFluxoCaixaUseCase.editar(id, new EditarCategoriaFluxoCaixaUseCase.Comando(request.nome()));
        return CategoriaFluxoCaixaResponse.de(categoriaFluxoCaixa);
    }

    @PatchMapping("/{id}/status")
    public CategoriaFluxoCaixaResponse alterarStatus(@PathVariable UUID id, @Valid @RequestBody AlterarStatusCategoriaFluxoCaixaRequest request) {
        CategoriaFluxoCaixa categoriaFluxoCaixa = alterarStatusCategoriaFluxoCaixaUseCase.alterarStatus(id, request.status());
        return CategoriaFluxoCaixaResponse.de(categoriaFluxoCaixa);
    }
}
