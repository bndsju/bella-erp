package com.bella.backend.adapter.in.web.categoria;

import com.bella.backend.domain.categoria.model.Categoria;
import com.bella.backend.domain.categoria.model.StatusCategoria;
import com.bella.backend.domain.categoria.port.in.AlterarStatusCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.BuscarCategoriaPorIdUseCase;
import com.bella.backend.domain.categoria.port.in.CadastrarCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.EditarCategoriaUseCase;
import com.bella.backend.domain.categoria.port.in.ListarCategoriasUseCase;
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
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CadastrarCategoriaUseCase cadastrarCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase;
    private final ListarCategoriasUseCase listarCategoriasUseCase;
    private final AlterarStatusCategoriaUseCase alterarStatusCategoriaUseCase;

    public CategoriaController(
            CadastrarCategoriaUseCase cadastrarCategoriaUseCase,
            EditarCategoriaUseCase editarCategoriaUseCase,
            BuscarCategoriaPorIdUseCase buscarCategoriaPorIdUseCase,
            ListarCategoriasUseCase listarCategoriasUseCase,
            AlterarStatusCategoriaUseCase alterarStatusCategoriaUseCase
    ) {
        this.cadastrarCategoriaUseCase = cadastrarCategoriaUseCase;
        this.editarCategoriaUseCase = editarCategoriaUseCase;
        this.buscarCategoriaPorIdUseCase = buscarCategoriaPorIdUseCase;
        this.listarCategoriasUseCase = listarCategoriasUseCase;
        this.alterarStatusCategoriaUseCase = alterarStatusCategoriaUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = cadastrarCategoriaUseCase.cadastrar(new CadastrarCategoriaUseCase.Comando(request.nome()));
        return ResponseEntity.created(URI.create("/api/categorias/" + categoria.getId()))
                .body(CategoriaResponse.de(categoria));
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable UUID id) {
        return CategoriaResponse.de(buscarCategoriaPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<CategoriaResponse> listar(@RequestParam(required = false) StatusCategoria status) {
        return listarCategoriasUseCase.listar(status).stream().map(CategoriaResponse::de).toList();
    }

    @PutMapping("/{id}")
    public CategoriaResponse editar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = editarCategoriaUseCase.editar(id, new EditarCategoriaUseCase.Comando(request.nome()));
        return CategoriaResponse.de(categoria);
    }

    @PatchMapping("/{id}/status")
    public CategoriaResponse alterarStatus(@PathVariable UUID id, @Valid @RequestBody AlterarStatusCategoriaRequest request) {
        Categoria categoria = alterarStatusCategoriaUseCase.alterarStatus(id, request.status());
        return CategoriaResponse.de(categoria);
    }
}
