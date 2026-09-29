package com.bella.backend.adapter.in.web.estoque;

import com.bella.backend.domain.estoque.model.MovimentacaoEstoque;
import com.bella.backend.domain.estoque.model.PosicaoEstoqueProduto;
import com.bella.backend.domain.estoque.model.TipoMovimentacaoEstoque;
import com.bella.backend.domain.estoque.port.in.ConsultarSaldoEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.ListarMovimentacoesEstoqueUseCase.FiltroListagem;
import com.bella.backend.domain.estoque.port.in.ListarProdutosAbaixoDoEstoqueMinimoUseCase;
import com.bella.backend.domain.estoque.port.in.ListarSaldoEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarAjusteEntradaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarAjusteSaidaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarEntradaEstoqueUseCase;
import com.bella.backend.domain.estoque.port.in.RegistrarSaidaEstoqueUseCase;
import com.bella.backend.domain.produto.model.Produto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final RegistrarEntradaEstoqueUseCase registrarEntradaEstoqueUseCase;
    private final RegistrarSaidaEstoqueUseCase registrarSaidaEstoqueUseCase;
    private final RegistrarAjusteEntradaEstoqueUseCase registrarAjusteEntradaEstoqueUseCase;
    private final RegistrarAjusteSaidaEstoqueUseCase registrarAjusteSaidaEstoqueUseCase;
    private final ConsultarSaldoEstoqueUseCase consultarSaldoEstoqueUseCase;
    private final ListarSaldoEstoqueUseCase listarSaldoEstoqueUseCase;
    private final ListarProdutosAbaixoDoEstoqueMinimoUseCase listarProdutosAbaixoDoEstoqueMinimoUseCase;
    private final ListarMovimentacoesEstoqueUseCase listarMovimentacoesEstoqueUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    public EstoqueController(
            RegistrarEntradaEstoqueUseCase registrarEntradaEstoqueUseCase,
            RegistrarSaidaEstoqueUseCase registrarSaidaEstoqueUseCase,
            RegistrarAjusteEntradaEstoqueUseCase registrarAjusteEntradaEstoqueUseCase,
            RegistrarAjusteSaidaEstoqueUseCase registrarAjusteSaidaEstoqueUseCase,
            ConsultarSaldoEstoqueUseCase consultarSaldoEstoqueUseCase,
            ListarSaldoEstoqueUseCase listarSaldoEstoqueUseCase,
            ListarProdutosAbaixoDoEstoqueMinimoUseCase listarProdutosAbaixoDoEstoqueMinimoUseCase,
            ListarMovimentacoesEstoqueUseCase listarMovimentacoesEstoqueUseCase,
            BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase
    ) {
        this.registrarEntradaEstoqueUseCase = registrarEntradaEstoqueUseCase;
        this.registrarSaidaEstoqueUseCase = registrarSaidaEstoqueUseCase;
        this.registrarAjusteEntradaEstoqueUseCase = registrarAjusteEntradaEstoqueUseCase;
        this.registrarAjusteSaidaEstoqueUseCase = registrarAjusteSaidaEstoqueUseCase;
        this.consultarSaldoEstoqueUseCase = consultarSaldoEstoqueUseCase;
        this.listarSaldoEstoqueUseCase = listarSaldoEstoqueUseCase;
        this.listarProdutosAbaixoDoEstoqueMinimoUseCase = listarProdutosAbaixoDoEstoqueMinimoUseCase;
        this.listarMovimentacoesEstoqueUseCase = listarMovimentacoesEstoqueUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
    }

    @PostMapping("/entradas")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarEntrada(
            @Valid @RequestBody MovimentacaoManualRequest request) {
        MovimentacaoEstoque movimentacao = registrarEntradaEstoqueUseCase.registrarEntrada(request.paraComando());
        return ResponseEntity.created(URI.create("/api/estoque/movimentacoes/" + movimentacao.getId()))
                .body(paraResponse(movimentacao));
    }

    @PostMapping("/saidas")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarSaida(
            @Valid @RequestBody MovimentacaoManualRequest request) {
        MovimentacaoEstoque movimentacao = registrarSaidaEstoqueUseCase.registrarSaida(request.paraComando());
        return ResponseEntity.created(URI.create("/api/estoque/movimentacoes/" + movimentacao.getId()))
                .body(paraResponse(movimentacao));
    }

    @PostMapping("/ajustes/entrada")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarAjusteEntrada(
            @Valid @RequestBody MovimentacaoManualRequest request) {
        MovimentacaoEstoque movimentacao =
                registrarAjusteEntradaEstoqueUseCase.registrarAjusteEntrada(request.paraComando());
        return ResponseEntity.created(URI.create("/api/estoque/movimentacoes/" + movimentacao.getId()))
                .body(paraResponse(movimentacao));
    }

    @PostMapping("/ajustes/saida")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarAjusteSaida(
            @Valid @RequestBody MovimentacaoManualRequest request) {
        MovimentacaoEstoque movimentacao =
                registrarAjusteSaidaEstoqueUseCase.registrarAjusteSaida(request.paraComando());
        return ResponseEntity.created(URI.create("/api/estoque/movimentacoes/" + movimentacao.getId()))
                .body(paraResponse(movimentacao));
    }

    @GetMapping("/produtos/{produtoId}")
    public SaldoEstoqueResponse buscarSaldoPorProduto(@PathVariable UUID produtoId) {
        PosicaoEstoqueProduto posicao = consultarSaldoEstoqueUseCase.buscarPorProdutoId(produtoId);
        return SaldoEstoqueResponse.de(posicao, buscarProdutoPorIdUseCase.buscarPorId(produtoId));
    }

    @GetMapping
    public List<SaldoEstoqueResponse> listarSaldoGeral() {
        return listarSaldoEstoqueUseCase.listarTodos().stream().map(this::paraResponse).toList();
    }

    @GetMapping("/abaixo-do-minimo")
    public List<SaldoEstoqueResponse> listarAbaixoDoMinimo() {
        return listarProdutosAbaixoDoEstoqueMinimoUseCase.listarAbaixoDoMinimo().stream()
                .map(this::paraResponse)
                .toList();
    }

    @GetMapping("/movimentacoes")
    public List<MovimentacaoEstoqueResponse> listarMovimentacoes(
            @RequestParam(required = false) UUID produtoId,
            @RequestParam(required = false) TipoMovimentacaoEstoque tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dataFim) {
        return listarMovimentacoesEstoqueUseCase.listar(new FiltroListagem(produtoId, tipo, dataInicio, dataFim))
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    private MovimentacaoEstoqueResponse paraResponse(MovimentacaoEstoque movimentacao) {
        Produto produto = buscarProdutoPorIdUseCase.buscarPorId(movimentacao.getProdutoId());
        return MovimentacaoEstoqueResponse.de(movimentacao, produto);
    }

    private SaldoEstoqueResponse paraResponse(PosicaoEstoqueProduto posicao) {
        Produto produto = buscarProdutoPorIdUseCase.buscarPorId(posicao.produtoId());
        return SaldoEstoqueResponse.de(posicao, produto);
    }
}
