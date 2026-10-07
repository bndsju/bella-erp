package com.bella.backend.adapter.in.web.categoriadespesa;

import com.bella.backend.domain.categoriadespesa.model.CategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.port.in.AlterarStatusCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.BuscarCategoriaDespesaPorIdUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.CadastrarCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.EditarCategoriaDespesaUseCase;
import com.bella.backend.domain.categoriadespesa.port.in.ListarCategoriaDespesasUseCase;
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
@RequestMapping("/api/categoriasDespesa-despesa-despesa")
public class CategoriaDespesaController {

    private final CadastrarCategoriaDespesaUseCase cadastrarCategoriaDespesaUseCase;
    private final EditarCategoriaDespesaUseCase editarCategoriaDespesaUseCase;
    private final BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase;
    private final ListarCategoriaDespesasUseCase listarCategoriaDespesasUseCase;
    private final AlterarStatusCategoriaDespesaUseCase alterarStatusCategoriaDespesaUseCase;

    public CategoriaDespesaController(
            CadastrarCategoriaDespesaUseCase cadastrarCategoriaDespesaUseCase,
            EditarCategoriaDespesaUseCase editarCategoriaDespesaUseCase,
            BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase,
            ListarCategoriaDespesasUseCase listarCategoriaDespesasUseCase,
            AlterarStatusCategoriaDespesaUseCase alterarStatusCategoriaDespesaUseCase
    ) {
        this.cadastrarCategoriaDespesaUseCase = cadastrarCategoriaDespesaUseCase;
        this.editarCategoriaDespesaUseCase = editarCategoriaDespesaUseCase;
        this.buscarCategoriaDespesaPorIdUseCase = buscarCategoriaDespesaPorIdUseCase;
        this.listarCategoriaDespesasUseCase = listarCategoriaDespesasUseCase;
        this.alterarStatusCategoriaDespesaUseCase = alterarStatusCategoriaDespesaUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoriaDespesaResponse> cadastrar(@Valid @RequestBody CategoriaDespesaRequest request) {
        CategoriaDespesa categoriaDespesa = cadastrarCategoriaDespesaUseCase.cadastrar(new CadastrarCategoriaDespesaUseCase.Comando(request.nome()));
        return ResponseEntity.created(URI.create("/api/categoriasDespesa-despesa-despesa/" + categoriaDespesa.getId()))
                .body(CategoriaDespesaResponse.de(categoriaDespesa));
    }

    @GetMapping("/{id}")
    public CategoriaDespesaResponse buscarPorId(@PathVariable UUID id) {
        return CategoriaDespesaResponse.de(buscarCategoriaDespesaPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<CategoriaDespesaResponse> listar(@RequestParam(required = false) StatusCategoriaDespesa status) {
        return listarCategoriaDespesasUseCase.listar(status).stream().map(CategoriaDespesaResponse::de).toList();
    }

    @PutMapping("/{id}")
    public CategoriaDespesaResponse editar(@PathVariable UUID id, @Valid @RequestBody CategoriaDespesaRequest request) {
        CategoriaDespesa categoriaDespesa = editarCategoriaDespesaUseCase.editar(id, new EditarCategoriaDespesaUseCase.Comando(request.nome()));
        return CategoriaDespesaResponse.de(categoriaDespesa);
    }

    @PatchMapping("/{id}/status")
    public CategoriaDespesaResponse alterarStatus(@PathVariable UUID id, @Valid @RequestBody AlterarStatusCategoriaDespesaRequest request) {
        CategoriaDespesa categoriaDespesa = alterarStatusCategoriaDespesaUseCase.alterarStatus(id, request.status());
        return CategoriaDespesaResponse.de(categoriaDespesa);
    }
}
