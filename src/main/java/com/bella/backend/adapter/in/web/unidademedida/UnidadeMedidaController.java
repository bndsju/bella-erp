package com.bella.backend.adapter.in.web.unidademedida;

import com.bella.backend.domain.unidademedida.model.StatusUnidadeMedida;
import com.bella.backend.domain.unidademedida.model.UnidadeMedida;
import com.bella.backend.domain.unidademedida.port.in.AlterarStatusUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.BuscarUnidadeMedidaPorIdUseCase;
import com.bella.backend.domain.unidademedida.port.in.CadastrarUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.EditarUnidadeMedidaUseCase;
import com.bella.backend.domain.unidademedida.port.in.ListarUnidadesMedidaUseCase;
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
@RequestMapping("/api/unidades-medida")
public class UnidadeMedidaController {

    private final CadastrarUnidadeMedidaUseCase cadastrarUnidadeMedidaUseCase;
    private final EditarUnidadeMedidaUseCase editarUnidadeMedidaUseCase;
    private final BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase;
    private final ListarUnidadesMedidaUseCase listarUnidadesMedidaUseCase;
    private final AlterarStatusUnidadeMedidaUseCase alterarStatusUnidadeMedidaUseCase;

    public UnidadeMedidaController(
            CadastrarUnidadeMedidaUseCase cadastrarUnidadeMedidaUseCase,
            EditarUnidadeMedidaUseCase editarUnidadeMedidaUseCase,
            BuscarUnidadeMedidaPorIdUseCase buscarUnidadeMedidaPorIdUseCase,
            ListarUnidadesMedidaUseCase listarUnidadesMedidaUseCase,
            AlterarStatusUnidadeMedidaUseCase alterarStatusUnidadeMedidaUseCase
    ) {
        this.cadastrarUnidadeMedidaUseCase = cadastrarUnidadeMedidaUseCase;
        this.editarUnidadeMedidaUseCase = editarUnidadeMedidaUseCase;
        this.buscarUnidadeMedidaPorIdUseCase = buscarUnidadeMedidaPorIdUseCase;
        this.listarUnidadesMedidaUseCase = listarUnidadesMedidaUseCase;
        this.alterarStatusUnidadeMedidaUseCase = alterarStatusUnidadeMedidaUseCase;
    }

    @PostMapping
    public ResponseEntity<UnidadeMedidaResponse> cadastrar(@Valid @RequestBody UnidadeMedidaRequest request) {
        UnidadeMedida unidadeMedida = cadastrarUnidadeMedidaUseCase.cadastrar(
                new CadastrarUnidadeMedidaUseCase.Comando(request.nome(), request.sigla()));
        return ResponseEntity.created(URI.create("/api/unidades-medida/" + unidadeMedida.getId()))
                .body(UnidadeMedidaResponse.de(unidadeMedida));
    }

    @GetMapping("/{id}")
    public UnidadeMedidaResponse buscarPorId(@PathVariable UUID id) {
        return UnidadeMedidaResponse.de(buscarUnidadeMedidaPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<UnidadeMedidaResponse> listar(@RequestParam(required = false) StatusUnidadeMedida status) {
        return listarUnidadesMedidaUseCase.listar(status).stream().map(UnidadeMedidaResponse::de).toList();
    }

    @PutMapping("/{id}")
    public UnidadeMedidaResponse editar(@PathVariable UUID id, @Valid @RequestBody UnidadeMedidaRequest request) {
        UnidadeMedida unidadeMedida = editarUnidadeMedidaUseCase.editar(
                id, new EditarUnidadeMedidaUseCase.Comando(request.nome(), request.sigla()));
        return UnidadeMedidaResponse.de(unidadeMedida);
    }

    @PatchMapping("/{id}/status")
    public UnidadeMedidaResponse alterarStatus(@PathVariable UUID id,
                                                @Valid @RequestBody AlterarStatusUnidadeMedidaRequest request) {
        UnidadeMedida unidadeMedida = alterarStatusUnidadeMedidaUseCase.alterarStatus(id, request.status());
        return UnidadeMedidaResponse.de(unidadeMedida);
    }
}
