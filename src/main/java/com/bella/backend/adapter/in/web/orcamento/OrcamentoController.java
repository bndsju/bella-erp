package com.bella.backend.adapter.in.web.orcamento;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.StatusOrcamento;
import com.bella.backend.domain.orcamento.port.in.AprovarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.BuscarOrcamentoPorIdUseCase;
import com.bella.backend.domain.orcamento.port.in.CadastrarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.CancelarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.EditarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.EnviarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase.FiltroListagem;
import com.bella.backend.domain.orcamento.port.in.RecusarOrcamentoUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
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
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    private final CadastrarOrcamentoUseCase cadastrarOrcamentoUseCase;
    private final EditarOrcamentoUseCase editarOrcamentoUseCase;
    private final BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase;
    private final ListarOrcamentosUseCase listarOrcamentosUseCase;
    private final EnviarOrcamentoUseCase enviarOrcamentoUseCase;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    private final RecusarOrcamentoUseCase recusarOrcamentoUseCase;
    private final CancelarOrcamentoUseCase cancelarOrcamentoUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    public OrcamentoController(
            CadastrarOrcamentoUseCase cadastrarOrcamentoUseCase,
            EditarOrcamentoUseCase editarOrcamentoUseCase,
            BuscarOrcamentoPorIdUseCase buscarOrcamentoPorIdUseCase,
            ListarOrcamentosUseCase listarOrcamentosUseCase,
            EnviarOrcamentoUseCase enviarOrcamentoUseCase,
            AprovarOrcamentoUseCase aprovarOrcamentoUseCase,
            RecusarOrcamentoUseCase recusarOrcamentoUseCase,
            CancelarOrcamentoUseCase cancelarOrcamentoUseCase,
            BuscarClientePorIdUseCase buscarClientePorIdUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase
    ) {
        this.cadastrarOrcamentoUseCase = cadastrarOrcamentoUseCase;
        this.editarOrcamentoUseCase = editarOrcamentoUseCase;
        this.buscarOrcamentoPorIdUseCase = buscarOrcamentoPorIdUseCase;
        this.listarOrcamentosUseCase = listarOrcamentosUseCase;
        this.enviarOrcamentoUseCase = enviarOrcamentoUseCase;
        this.aprovarOrcamentoUseCase = aprovarOrcamentoUseCase;
        this.recusarOrcamentoUseCase = recusarOrcamentoUseCase;
        this.cancelarOrcamentoUseCase = cancelarOrcamentoUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<OrcamentoResponse> cadastrar(@Valid @RequestBody OrcamentoRequest request) {
        Orcamento orcamento = cadastrarOrcamentoUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/orcamentos/" + orcamento.getId()))
                .body(paraResponse(orcamento));
    }

    @GetMapping("/{id}")
    public OrcamentoResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarOrcamentoPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<OrcamentoResponse> listar(@RequestParam(required = false) UUID clienteId,
                                           @RequestParam(required = false) StatusOrcamento status) {
        return listarOrcamentosUseCase.listar(new FiltroListagem(clienteId, status)).stream()
                .map(this::paraResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public OrcamentoResponse editar(@PathVariable UUID id, @Valid @RequestBody OrcamentoRequest request) {
        Orcamento orcamento = editarOrcamentoUseCase.editar(id, request.paraComando());
        return paraResponse(orcamento);
    }

    @PatchMapping("/{id}/enviar")
    public OrcamentoResponse enviar(@PathVariable UUID id) {
        return paraResponse(enviarOrcamentoUseCase.enviar(id));
    }

    @PatchMapping("/{id}/aprovar")
    public OrcamentoResponse aprovar(@PathVariable UUID id) {
        return paraResponse(aprovarOrcamentoUseCase.aprovar(id));
    }

    @PatchMapping("/{id}/recusar")
    public OrcamentoResponse recusar(@PathVariable UUID id) {
        return paraResponse(recusarOrcamentoUseCase.recusar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public OrcamentoResponse cancelar(@PathVariable UUID id) {
        return paraResponse(cancelarOrcamentoUseCase.cancelar(id));
    }

    private OrcamentoResponse paraResponse(Orcamento orcamento) {
        Cliente cliente = buscarClientePorIdUseCase.buscarPorId(orcamento.getClienteId());

        List<OrcamentoItemResponse> itens = orcamento.getItens().stream()
                .map(item -> OrcamentoItemResponse.de(item, buscarProdutoPorIdUseCase.buscarPorId(item.getProdutoId())))
                .toList();

        return OrcamentoResponse.de(orcamento, cliente, itens);
    }
}
