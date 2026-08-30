package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.StatusCliente;
import com.bella.backend.domain.cliente.port.in.AlterarStatusClienteUseCase;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.cliente.port.in.CadastrarClienteUseCase;
import com.bella.backend.domain.cliente.port.in.EditarClienteUseCase;
import com.bella.backend.domain.cliente.port.in.ListarClientesUseCase;
import com.bella.backend.domain.cliente.port.in.ListarClientesUseCase.FiltroListagem;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final EditarClienteUseCase editarClienteUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final AlterarStatusClienteUseCase alterarStatusClienteUseCase;

    public ClienteController(
            CadastrarClienteUseCase cadastrarClienteUseCase,
            EditarClienteUseCase editarClienteUseCase,
            BuscarClientePorIdUseCase buscarClientePorIdUseCase,
            ListarClientesUseCase listarClientesUseCase,
            AlterarStatusClienteUseCase alterarStatusClienteUseCase
    ) {
        this.cadastrarClienteUseCase = cadastrarClienteUseCase;
        this.editarClienteUseCase = editarClienteUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
        this.alterarStatusClienteUseCase = alterarStatusClienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
        Cliente cliente = cadastrarClienteUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/clientes/" + cliente.getId()))
                .body(ClienteResponse.de(cliente));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable UUID id) {
        return ClienteResponse.de(buscarClientePorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestParam(required = false) String busca,
                                         @RequestParam(required = false) StatusCliente status) {
        return listarClientesUseCase.listar(new FiltroListagem(busca, status)).stream()
                .map(ClienteResponse::de)
                .toList();
    }

    @PutMapping("/{id}")
    public ClienteResponse editar(@PathVariable UUID id, @Valid @RequestBody ClienteRequest request) {
        Cliente cliente = editarClienteUseCase.editar(id, request.paraComando());
        return ClienteResponse.de(cliente);
    }

    @PatchMapping("/{id}/status")
    public ClienteResponse alterarStatus(@PathVariable UUID id, @Valid @RequestBody AlterarStatusRequest request) {
        Cliente cliente = alterarStatusClienteUseCase.alterarStatus(id, request.status());
        return ClienteResponse.de(cliente);
    }
}
