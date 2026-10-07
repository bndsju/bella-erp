package com.bella.backend.adapter.in.web.fornecedor;

import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.AlterarStatusFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.fornecedor.port.in.CadastrarFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.EditarFornecedorUseCase;
import com.bella.backend.domain.fornecedor.port.in.ListarFornecedoresUseCase;
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
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final CadastrarFornecedorUseCase cadastrarFornecedorUseCase;
    private final EditarFornecedorUseCase editarFornecedorUseCase;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final ListarFornecedoresUseCase listarFornecedoresUseCase;
    private final AlterarStatusFornecedorUseCase alterarStatusFornecedorUseCase;

    public FornecedorController(
            CadastrarFornecedorUseCase cadastrarFornecedorUseCase,
            EditarFornecedorUseCase editarFornecedorUseCase,
            BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase,
            ListarFornecedoresUseCase listarFornecedoresUseCase,
            AlterarStatusFornecedorUseCase alterarStatusFornecedorUseCase
    ) {
        this.cadastrarFornecedorUseCase = cadastrarFornecedorUseCase;
        this.editarFornecedorUseCase = editarFornecedorUseCase;
        this.buscarFornecedorPorIdUseCase = buscarFornecedorPorIdUseCase;
        this.listarFornecedoresUseCase = listarFornecedoresUseCase;
        this.alterarStatusFornecedorUseCase = alterarStatusFornecedorUseCase;
    }

    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(@Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = cadastrarFornecedorUseCase.cadastrar(
                new CadastrarFornecedorUseCase.Comando(request.nome(), request.telefone()));
        return ResponseEntity.created(URI.create("/api/fornecedores/" + fornecedor.getId()))
                .body(FornecedorResponse.de(fornecedor));
    }

    @GetMapping("/{id}")
    public FornecedorResponse buscarPorId(@PathVariable UUID id) {
        return FornecedorResponse.de(buscarFornecedorPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<FornecedorResponse> listar(@RequestParam(required = false) StatusFornecedor status) {
        return listarFornecedoresUseCase.listar(status).stream().map(FornecedorResponse::de).toList();
    }

    @PutMapping("/{id}")
    public FornecedorResponse editar(@PathVariable UUID id, @Valid @RequestBody FornecedorRequest request) {
        Fornecedor fornecedor = editarFornecedorUseCase.editar(
                id, new EditarFornecedorUseCase.Comando(request.nome(), request.telefone()));
        return FornecedorResponse.de(fornecedor);
    }

    @PatchMapping("/{id}/status")
    public FornecedorResponse alterarStatus(@PathVariable UUID id,
                                                 @Valid @RequestBody AlterarStatusFornecedorRequest request) {
        Fornecedor fornecedor = alterarStatusFornecedorUseCase.alterarStatus(id, request.status());
        return FornecedorResponse.de(fornecedor);
    }
}
