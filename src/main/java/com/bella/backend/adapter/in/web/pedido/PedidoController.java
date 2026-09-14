package com.bella.backend.adapter.in.web.pedido;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.BuscarPedidoPorIdUseCase;
import com.bella.backend.domain.pedido.port.in.CadastrarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.CancelarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.ConfirmarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.CriarPedidoAPartirDeOrcamentoUseCase;
import com.bella.backend.domain.pedido.port.in.EditarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.EntregarPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.IniciarSeparacaoPedidoUseCase;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase;
import com.bella.backend.domain.pedido.port.in.ListarPedidosUseCase.FiltroListagem;
import com.bella.backend.domain.pedido.port.in.MarcarProntoParaEntregaPedidoUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.transportadora.model.Transportadora;
import com.bella.backend.domain.transportadora.port.in.BuscarTransportadoraPorIdUseCase;
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
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final CadastrarPedidoUseCase cadastrarPedidoUseCase;
    private final CriarPedidoAPartirDeOrcamentoUseCase criarPedidoAPartirDeOrcamentoUseCase;
    private final EditarPedidoUseCase editarPedidoUseCase;
    private final BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;
    private final ListarPedidosUseCase listarPedidosUseCase;
    private final ConfirmarPedidoUseCase confirmarPedidoUseCase;
    private final IniciarSeparacaoPedidoUseCase iniciarSeparacaoPedidoUseCase;
    private final MarcarProntoParaEntregaPedidoUseCase marcarProntoParaEntregaPedidoUseCase;
    private final EntregarPedidoUseCase entregarPedidoUseCase;
    private final CancelarPedidoUseCase cancelarPedidoUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase;

    public PedidoController(
            CadastrarPedidoUseCase cadastrarPedidoUseCase,
            CriarPedidoAPartirDeOrcamentoUseCase criarPedidoAPartirDeOrcamentoUseCase,
            EditarPedidoUseCase editarPedidoUseCase,
            BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase,
            ListarPedidosUseCase listarPedidosUseCase,
            ConfirmarPedidoUseCase confirmarPedidoUseCase,
            IniciarSeparacaoPedidoUseCase iniciarSeparacaoPedidoUseCase,
            MarcarProntoParaEntregaPedidoUseCase marcarProntoParaEntregaPedidoUseCase,
            EntregarPedidoUseCase entregarPedidoUseCase,
            CancelarPedidoUseCase cancelarPedidoUseCase,
            BuscarClientePorIdUseCase buscarClientePorIdUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
            BuscarTransportadoraPorIdUseCase buscarTransportadoraPorIdUseCase
    ) {
        this.cadastrarPedidoUseCase = cadastrarPedidoUseCase;
        this.criarPedidoAPartirDeOrcamentoUseCase = criarPedidoAPartirDeOrcamentoUseCase;
        this.editarPedidoUseCase = editarPedidoUseCase;
        this.buscarPedidoPorIdUseCase = buscarPedidoPorIdUseCase;
        this.listarPedidosUseCase = listarPedidosUseCase;
        this.confirmarPedidoUseCase = confirmarPedidoUseCase;
        this.iniciarSeparacaoPedidoUseCase = iniciarSeparacaoPedidoUseCase;
        this.marcarProntoParaEntregaPedidoUseCase = marcarProntoParaEntregaPedidoUseCase;
        this.entregarPedidoUseCase = entregarPedidoUseCase;
        this.cancelarPedidoUseCase = cancelarPedidoUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.buscarTransportadoraPorIdUseCase = buscarTransportadoraPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> cadastrar(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = cadastrarPedidoUseCase.cadastrar(request.paraComando());
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.getId())).body(paraResponse(pedido));
    }

    @PostMapping("/a-partir-de-orcamento")
    public ResponseEntity<PedidoResponse> criarAPartirDeOrcamento(
            @Valid @RequestBody CriarPedidoDeOrcamentoRequest request) {
        Pedido pedido = criarPedidoAPartirDeOrcamentoUseCase.criarAPartirDeOrcamento(
                request.orcamentoId(), request.transportadoraId());
        return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.getId())).body(paraResponse(pedido));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscarPorId(@PathVariable UUID id) {
        return paraResponse(buscarPedidoPorIdUseCase.buscarPorId(id));
    }

    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) UUID clienteId,
                                        @RequestParam(required = false) StatusPedido status) {
        return listarPedidosUseCase.listar(new FiltroListagem(clienteId, status)).stream()
                .map(this::paraResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public PedidoResponse editar(@PathVariable UUID id, @Valid @RequestBody PedidoRequest request) {
        Pedido pedido = editarPedidoUseCase.editar(id, request.paraComando());
        return paraResponse(pedido);
    }

    @PatchMapping("/{id}/confirmar")
    public PedidoResponse confirmar(@PathVariable UUID id) {
        return paraResponse(confirmarPedidoUseCase.confirmar(id));
    }

    @PatchMapping("/{id}/iniciar-separacao")
    public PedidoResponse iniciarSeparacao(@PathVariable UUID id) {
        return paraResponse(iniciarSeparacaoPedidoUseCase.iniciarSeparacao(id));
    }

    @PatchMapping("/{id}/marcar-pronto-para-entrega")
    public PedidoResponse marcarProntoParaEntrega(@PathVariable UUID id) {
        return paraResponse(marcarProntoParaEntregaPedidoUseCase.marcarProntoParaEntrega(id));
    }

    @PatchMapping("/{id}/entregar")
    public PedidoResponse entregar(@PathVariable UUID id) {
        return paraResponse(entregarPedidoUseCase.entregar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@PathVariable UUID id) {
        return paraResponse(cancelarPedidoUseCase.cancelar(id));
    }

    private PedidoResponse paraResponse(Pedido pedido) {
        Cliente cliente = buscarClientePorIdUseCase.buscarPorId(pedido.getClienteId());
        Transportadora transportadora = pedido.getTransportadoraId() == null
                ? null
                : buscarTransportadoraPorIdUseCase.buscarPorId(pedido.getTransportadoraId());

        List<PedidoItemResponse> itens = pedido.getItens().stream()
                .map(item -> PedidoItemResponse.de(item, buscarProdutoPorIdUseCase.buscarPorId(item.getProdutoId())))
                .toList();

        return PedidoResponse.de(pedido, cliente, transportadora, itens);
    }
}
