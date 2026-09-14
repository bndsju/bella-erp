package com.bella.backend.adapter.in.web.transportadora;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;
import com.bella.backend.domain.transportadora.port.in.AlterarStatusTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.BuscarTransportadoraPorIdUseCase;
import com.bella.backend.domain.transportadora.port.in.CadastrarTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.EditarTransportadoraUseCase;
import com.bella.backend.domain.transportadora.port.in.ListarTransportadorasUseCase;
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
@RequestMapping("/api/transportadoras")
public class TransportadoraController {

    private final CadastrarTransportadoraUseCase cadastrarTransportadoraUseCase;
    private final EditarTransportadoraUseCase editarTransportadoraUseCase;
    private final BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase;
    private final ListarTransportadorasUseCase listarTransportadorasUseCase;
    private final AlterarStatusTransportadoraUseCase alterarStatusTransportadoraUseCase;

    public TransportadoraController(
            CadastrarTransportadoraUseCase cadastrarTransportadoraUseCase,
            EditarTransportadoraUseCase editarTransportadoraUseCase,
            BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase,
            ListarTransportadorasUseCase listarTransportadorasUseCase,
            AlterarStatusTransportadoraUseCase alterarStatusTransportadoraUseCase
    ) {
        this.cadastrarTransportadoraUseCase = cadastrarTransportadoraUseCase;
        this.editarTransportadoraUseCase = editarTransportadoraUseCase;
        this.buscarTransportadoraPorIdUseCase = buscarTransportadoraPorIdUseCase;
        this.listarTransportadorasUseCase = listarTransportadorasUseCase;
        this.alterarStatusTransportadoraUseCase = alterarStatusTransportadoraUseCase;
    }

    @PostMapping
    public ResponseEntity<TransportadoraResponse> cadastrar(@Valid @RequestBody TransportadoraRequest request) {
        Transportadora transportadora = cadastrarTransportadoraUseCase.cadastrar(
                new CadastrarTransportadoraUseCase.Comando(request.nome(), request.telefone()));
        return ResponseEntity.created(URI.create("/api/transportadoras/" + transportadora.getId()))
                .body(TransportadoraResponse.de(transportadora));
    }

    @GetMapping("/{id}")
    public TransportadoraResponse buscarPorId(@PathVariable UUID id) {
        return TransportadoraResponse.de(buscarTransportadoraPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<TransportadoraResponse> listar(@RequestParam(required = false) StatusTransportadora status) {
        return listarTransportadorasUseCase.listar(status).stream().map(TransportadoraResponse::de).toList();
    }

    @PutMapping("/{id}")
    public TransportadoraResponse editar(@PathVariable UUID id, @Valid @RequestBody TransportadoraRequest request) {
        Transportadora transportadora = editarTransportadoraUseCase.editar(
                id, new EditarTransportadoraUseCase.Comando(request.nome(), request.telefone()));
        return TransportadoraResponse.de(transportadora);
    }

    @PatchMapping("/{id}/status")
    public TransportadoraResponse alterarStatus(@PathVariable UUID id,
                                                 @Valid @RequestBody AlterarStatusTransportadoraRequest request) {
        Transportadora transportadora = alterarStatusTransportadoraUseCase.alterarStatus(id, request.status());
        return TransportadoraResponse.de(transportadora);
    }
}
