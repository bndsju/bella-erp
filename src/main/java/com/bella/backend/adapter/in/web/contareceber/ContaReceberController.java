package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.contareceber.port.in.BuscarContaReceberPorIdUseCase;
import com.bella.backend.domain.contareceber.port.in.CadastrarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.CancelarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.EditarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.EditarDadosBasicosContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.ListarContasReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.ListarContasReceberUseCase.FiltroListagem;
import com.bella.backend.domain.contareceber.port.in.RegistrarRecebimentoContaReceberUseCase;
import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contas-receber")
public class ContaReceberController {

    private final CadastrarContaReceberUseCase cadastrarContaReceberUseCase;
    private final EditarContaReceberUseCase editarContaReceberUseCase;
    private final EditarDadosBasicosContaReceberUseCase editarDadosBasicosContaReceberUseCase;
    private final BuscarContaReceberPorIdUseCase buscarContaReceberPorIdUseCase;
    private final ListarContasReceberUseCase listarContasReceberUseCase;
    private final RegistrarRecebimentoContaReceberUseCase registrarRecebimentoContaReceberUseCase;
    private final CancelarContaReceberUseCase cancelarContaReceberUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    public ContaReceberController(
            CadastrarContaReceberUseCase cadastrarContaReceberUseCase,
            EditarContaReceberUseCase editarContaReceberUseCase,
            EditarDadosBasicosContaReceberUseCase editarDadosBasicosContaReceberUseCase,
            BuscarContaReceberPorIdUseCase buscarContaReceberPorIdUseCase,
            ListarContasReceberUseCase listarContasReceberUseCase,
            RegistrarRecebimentoContaReceberUseCase registrarRecebimentoContaReceberUseCase,
            CancelarContaReceberUseCase cancelarContaReceberUseCase,
            BuscarClientePorIdUseCase buscarClientePorIdUseCase
    ) {
        this.cadastrarContaReceberUseCase = cadastrarContaReceberUseCase;
        this.editarContaReceberUseCase = editarContaReceberUseCase;
        this.editarDadosBasicosContaReceberUseCase = editarDadosBasicosContaReceberUseCase;
        this.buscarContaReceberPorIdUseCase = buscarContaReceberPorIdUseCase;
        this.listarContasReceberUseCase = listarContasReceberUseCase;
        this.registrarRecebimentoContaReceberUseCase = registrarRecebimentoContaReceberUseCase;
        this.cancelarContaReceberUseCase = cancelarContaReceberUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<ContaReceberResponse> cadastrar(@Valid @RequestBody ContaReceberRequest request) {
        ContaReceber conta = cadastrarContaReceberUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/contas-receber/" + conta.getId())).body(paraResponse(conta));
    }

    @GetMapping("/{id}")
    public ContaReceberResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarContaReceberPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<ContaReceberResponse> listar(
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) StatusContaReceber status,
            @RequestParam(required = false) SituacaoContaReceber situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoFim) {
        FiltroListagem filtro = new FiltroListagem(clienteId, status, situacao,
                vencimentoInicio, vencimentoFim);
        return listarContasReceberUseCase.listar(filtro).stream().map(this::paraResponse).toList();
    }

    @PutMapping("/{id}")
    public ContaReceberResponse editar(@PathVariable UUID id, @Valid @RequestBody ContaReceberRequest request) {
        return paraResponse(editarContaReceberUseCase.editar(id, request.paraComando()));
    }

    @PatchMapping("/{id}/dados-basicos")
    public ContaReceberResponse editarDadosBasicos(@PathVariable UUID id,
                                                  @Valid @RequestBody DadosBasicosContaReceberRequest request) {
        return paraResponse(editarDadosBasicosContaReceberUseCase.editarDadosBasicos(id, request.paraComando()));
    }

    @PostMapping("/{id}/recebimentos")
    public ResponseEntity<ContaReceberResponse> registrarRecebimento(
            @PathVariable UUID id, @Valid @RequestBody RecebimentoContaReceberRequest request) {
        ContaReceber conta = registrarRecebimentoContaReceberUseCase.registrarRecebimento(id, request.paraComando());
        return ResponseEntity.created(URI.create("/api/contas-receber/" + conta.getId())).body(paraResponse(conta));
    }

    @PatchMapping("/{id}/cancelar")
    public ContaReceberResponse cancelar(@PathVariable UUID id, @Valid @RequestBody CancelarContaReceberRequest request) {
        return paraResponse(cancelarContaReceberUseCase.cancelar(id, request.motivo()));
    }

    private ContaReceberResponse paraResponse(ContaReceber conta) {
        Cliente cliente = conta.getClienteId() == null
                ? null
                : buscarClientePorIdUseCase.buscarPorId(conta.getClienteId());
        return ContaReceberResponse.de(conta, cliente, LocalDate.now());
    }
}
