package com.bella.backend.adapter.in.web.contapagar;

import com.bella.backend.domain.categoriadespesa.port.in.BuscarCategoriaDespesaPorIdUseCase;
import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.contapagar.port.in.BuscarContaPagarPorIdUseCase;
import com.bella.backend.domain.contapagar.port.in.CadastrarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.CancelarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.EditarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.EditarDadosBasicosContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.ListarContasPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.ListarContasPagarUseCase.FiltroListagem;
import com.bella.backend.domain.contapagar.port.in.RegistrarPagamentoContaPagarUseCase;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
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
@RequestMapping("/api/contas-pagar")
public class ContaPagarController {

    private final CadastrarContaPagarUseCase cadastrarContaPagarUseCase;
    private final EditarContaPagarUseCase editarContaPagarUseCase;
    private final EditarDadosBasicosContaPagarUseCase editarDadosBasicosContaPagarUseCase;
    private final BuscarContaPagarPorIdUseCase buscarContaPagarPorIdUseCase;
    private final ListarContasPagarUseCase listarContasPagarUseCase;
    private final RegistrarPagamentoContaPagarUseCase registrarPagamentoContaPagarUseCase;
    private final CancelarContaPagarUseCase cancelarContaPagarUseCase;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase;

    public ContaPagarController(
            CadastrarContaPagarUseCase cadastrarContaPagarUseCase,
            EditarContaPagarUseCase editarContaPagarUseCase,
            EditarDadosBasicosContaPagarUseCase editarDadosBasicosContaPagarUseCase,
            BuscarContaPagarPorIdUseCase buscarContaPagarPorIdUseCase,
            ListarContasPagarUseCase listarContasPagarUseCase,
            RegistrarPagamentoContaPagarUseCase registrarPagamentoContaPagarUseCase,
            CancelarContaPagarUseCase cancelarContaPagarUseCase,
            BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase,
            BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase
    ) {
        this.cadastrarContaPagarUseCase = cadastrarContaPagarUseCase;
        this.editarContaPagarUseCase = editarContaPagarUseCase;
        this.editarDadosBasicosContaPagarUseCase = editarDadosBasicosContaPagarUseCase;
        this.buscarContaPagarPorIdUseCase = buscarContaPagarPorIdUseCase;
        this.listarContasPagarUseCase = listarContasPagarUseCase;
        this.registrarPagamentoContaPagarUseCase = registrarPagamentoContaPagarUseCase;
        this.cancelarContaPagarUseCase = cancelarContaPagarUseCase;
        this.buscarFornecedorPorIdUseCase = buscarFornecedorPorIdUseCase;
        this.buscarCategoriaDespesaPorIdUseCase = buscarCategoriaDespesaPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<ContaPagarResponse> cadastrar(@Valid @RequestBody ContaPagarRequest request) {
        ContaPagar conta = cadastrarContaPagarUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/contas-pagar/" + conta.getId())).body(paraResponse(conta));
    }

    @GetMapping("/{id}")
    public ContaPagarResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarContaPagarPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<ContaPagarResponse> listar(
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) UUID categoriaDespesaId,
            @RequestParam(required = false) StatusContaPagar status,
            @RequestParam(required = false) SituacaoContaPagar situacao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoFim) {
        FiltroListagem filtro = new FiltroListagem(fornecedorId, categoriaDespesaId, status, situacao,
                vencimentoInicio, vencimentoFim);
        return listarContasPagarUseCase.listar(filtro).stream().map(this::paraResponse).toList();
    }

    @PutMapping("/{id}")
    public ContaPagarResponse editar(@PathVariable UUID id, @Valid @RequestBody ContaPagarRequest request) {
        return paraResponse(editarContaPagarUseCase.editar(id, request.paraComando()));
    }

    @PatchMapping("/{id}/dados-basicos")
    public ContaPagarResponse editarDadosBasicos(@PathVariable UUID id,
                                                  @Valid @RequestBody DadosBasicosContaPagarRequest request) {
        return paraResponse(editarDadosBasicosContaPagarUseCase.editarDadosBasicos(id, request.paraComando()));
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<ContaPagarResponse> registrarPagamento(
            @PathVariable UUID id, @Valid @RequestBody PagamentoContaPagarRequest request) {
        ContaPagar conta = registrarPagamentoContaPagarUseCase.registrarPagamento(id, request.paraComando());
        return ResponseEntity.created(URI.create("/api/contas-pagar/" + conta.getId())).body(paraResponse(conta));
    }

    @PatchMapping("/{id}/cancelar")
    public ContaPagarResponse cancelar(@PathVariable UUID id, @Valid @RequestBody CancelarContaPagarRequest request) {
        return paraResponse(cancelarContaPagarUseCase.cancelar(id, request.motivo()));
    }

    private ContaPagarResponse paraResponse(ContaPagar conta) {
        Fornecedor fornecedor = conta.getFornecedorId() == null
                ? null
                : buscarFornecedorPorIdUseCase.buscarPorId(conta.getFornecedorId());
        return ContaPagarResponse.de(conta, fornecedor,
                buscarCategoriaDespesaPorIdUseCase.buscarPorId(conta.getCategoriaDespesaId()), LocalDate.now());
    }
}
