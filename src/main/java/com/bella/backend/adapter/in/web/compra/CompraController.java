package com.bella.backend.adapter.in.web.compra;

import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.compra.model.StatusCompra;
import com.bella.backend.domain.compra.port.in.BuscarCompraPorIdUseCase;
import com.bella.backend.domain.compra.port.in.CadastrarCompraUseCase;
import com.bella.backend.domain.compra.port.in.CancelarCompraUseCase;
import com.bella.backend.domain.compra.port.in.ConfirmarCompraUseCase;
import com.bella.backend.domain.compra.port.in.EditarCompraUseCase;
import com.bella.backend.domain.compra.port.in.ListarComprasPendentesRecebimentoUseCase;
import com.bella.backend.domain.compra.port.in.ListarComprasUseCase;
import com.bella.backend.domain.compra.port.in.ListarComprasUseCase.FiltroListagem;
import com.bella.backend.domain.compra.port.in.ReceberCompraUseCase;
import com.bella.backend.domain.fornecedor.model.Fornecedor;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
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
@RequestMapping("/api/compras")
public class CompraController {

    private final CadastrarCompraUseCase cadastrarCompraUseCase;
    private final EditarCompraUseCase editarCompraUseCase;
    private final BuscarCompraPorIdUseCase buscarCompraPorIdUseCase;
    private final ListarComprasUseCase listarComprasUseCase;
    private final ListarComprasPendentesRecebimentoUseCase listarComprasPendentesRecebimentoUseCase;
    private final ConfirmarCompraUseCase confirmarCompraUseCase;
    private final ReceberCompraUseCase receberCompraUseCase;
    private final CancelarCompraUseCase cancelarCompraUseCase;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    public CompraController(
            CadastrarCompraUseCase cadastrarCompraUseCase,
            EditarCompraUseCase editarCompraUseCase,
            BuscarCompraPorIdUseCase buscarCompraPorIdUseCase,
            ListarComprasUseCase listarComprasUseCase,
            ListarComprasPendentesRecebimentoUseCase listarComprasPendentesRecebimentoUseCase,
            ConfirmarCompraUseCase confirmarCompraUseCase,
            ReceberCompraUseCase receberCompraUseCase,
            CancelarCompraUseCase cancelarCompraUseCase,
            BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase
    ) {
        this.cadastrarCompraUseCase = cadastrarCompraUseCase;
        this.editarCompraUseCase = editarCompraUseCase;
        this.buscarCompraPorIdUseCase = buscarCompraPorIdUseCase;
        this.listarComprasUseCase = listarComprasUseCase;
        this.listarComprasPendentesRecebimentoUseCase = listarComprasPendentesRecebimentoUseCase;
        this.confirmarCompraUseCase = confirmarCompraUseCase;
        this.receberCompraUseCase = receberCompraUseCase;
        this.cancelarCompraUseCase = cancelarCompraUseCase;
        this.buscarFornecedorPorIdUseCase = buscarFornecedorPorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<CompraResponse> cadastrar(@Valid @RequestBody CompraRequest request) {
        Compra compra = cadastrarCompraUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/compras/" + compra.getId())).body(paraResponse(compra));
    }

    @GetMapping("/pendentes-recebimento")
    public List<CompraResponse> listarPendentesDeRecebimento() {
        return listarComprasPendentesRecebimentoUseCase.listarPendentesDeRecebimento().stream()
                .map(this::paraResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CompraResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarCompraPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<CompraResponse> listar(
            @RequestParam(required = false) UUID fornecedorId,
            @RequestParam(required = false) StatusCompra status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return listarComprasUseCase.listar(new FiltroListagem(fornecedorId, status, dataInicio, dataFim)).stream()
                .map(this::paraResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public CompraResponse editar(@PathVariable UUID id, @Valid @RequestBody CompraRequest request) {
        return paraResponse(editarCompraUseCase.editar(id, request.paraComando()));
    }

    @PatchMapping("/{id}/confirmar")
    public CompraResponse confirmar(@PathVariable UUID id) {
        return paraResponse(confirmarCompraUseCase.confirmar(id));
    }

    @PatchMapping("/{id}/receber")
    public CompraResponse receber(@PathVariable UUID id,
                                   @RequestBody(required = false) ReceberCompraRequest request) {
        LocalDate dataRecebimento = request == null ? null : request.dataRecebimento();
        return paraResponse(receberCompraUseCase.receber(id, dataRecebimento));
    }

    @PatchMapping("/{id}/cancelar")
    public CompraResponse cancelar(@PathVariable UUID id, @Valid @RequestBody CancelarCompraRequest request) {
        return paraResponse(cancelarCompraUseCase.cancelar(id, request.motivo()));
    }

    private CompraResponse paraResponse(Compra compra) {
        Fornecedor fornecedor = buscarFornecedorPorIdUseCase.buscarPorId(compra.getFornecedorId());

        List<CompraItemResponse> itens = compra.getItens().stream()
                .map(item -> CompraItemResponse.de(item, buscarProdutoPorIdUseCase.buscarPorId(item.getProdutoId())))
                .toList();

        return CompraResponse.de(compra, fornecedor, itens);
    }
}
