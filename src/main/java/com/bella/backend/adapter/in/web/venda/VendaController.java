package com.bella.backend.adapter.in.web.venda;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.venda.model.StatusVenda;
import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.port.in.BuscarVendaPorIdUseCase;
import com.bella.backend.domain.venda.port.in.CancelarVendaUseCase;
import com.bella.backend.domain.venda.port.in.ConcluirVendaUseCase;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase.FiltroListagem;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final ConcluirVendaUseCase concluirVendaUseCase;
    private final BuscarVendaPorIdUseCase buscarVendaPorIdUseCase;
    private final ListarVendasUseCase listarVendasUseCase;
    private final CancelarVendaUseCase cancelarVendaUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    public VendaController(
            ConcluirVendaUseCase concluirVendaUseCase,
            BuscarVendaPorIdUseCase buscarVendaPorIdUseCase,
            ListarVendasUseCase listarVendasUseCase,
            CancelarVendaUseCase cancelarVendaUseCase,
            BuscarClientePorIdUseCase buscarClientePorIdUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase
    ) {
        this.concluirVendaUseCase = concluirVendaUseCase;
        this.buscarVendaPorIdUseCase = buscarVendaPorIdUseCase;
        this.listarVendasUseCase = listarVendasUseCase;
        this.cancelarVendaUseCase = cancelarVendaUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<VendaResponse> concluir(@Valid @RequestBody ConcluirVendaRequest request) {
        Venda venda = concluirVendaUseCase.concluir(request.pedidoId(), request.formaPagamento());
        return ResponseEntity.created(URI.create("/api/vendas/" + venda.getId())).body(paraResponse(venda));
    }

    @GetMapping("/{id}")
    public VendaResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarVendaPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<VendaResponse> listar(@RequestParam(required = false) UUID clienteId,
                                       @RequestParam(required = false) StatusVenda status) {
        return listarVendasUseCase.listar(new FiltroListagem(clienteId, status)).stream()
                .map(this::paraResponse)
                .toList();
    }

    @PatchMapping("/{id}/cancelar")
    public VendaResponse cancelar(@PathVariable UUID id) {
        return paraResponse(cancelarVendaUseCase.cancelar(id));
    }

    private VendaResponse paraResponse(Venda venda) {
        Cliente cliente = buscarClientePorIdUseCase.buscarPorId(venda.getClienteId());

        List<VendaItemResponse> itens = venda.getItens().stream()
                .map(item -> VendaItemResponse.de(item, buscarProdutoPorIdUseCase.buscarPorId(item.getProdutoId())))
                .toList();

        return VendaResponse.de(venda, cliente, itens);
    }
}
