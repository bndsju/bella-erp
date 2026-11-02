package com.bella.backend.application.compra.usecase;

import com.bella.backend.domain.categoriadespesa.model.CategoriasDespesaDoSistema;
import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.port.in.CadastrarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.ComandoContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoParcelaContaPagar;
import com.bella.backend.domain.shared.CondicaoPagamentoParcelas;
import com.bella.backend.domain.compra.model.CompraItem;
import com.bella.backend.domain.compra.model.StatusCompra;
import com.bella.backend.domain.compra.port.in.BuscarCompraPorIdUseCase;
import com.bella.backend.domain.compra.port.in.CadastrarCompraUseCase;
import com.bella.backend.domain.compra.port.in.CancelarCompraUseCase;
import com.bella.backend.domain.compra.port.in.ComandoCompra;
import com.bella.backend.domain.compra.port.in.ComandoItemCompra;
import com.bella.backend.domain.compra.port.in.ConfirmarCompraUseCase;
import com.bella.backend.domain.compra.port.in.EditarCompraUseCase;
import com.bella.backend.domain.compra.port.in.ListarComprasPendentesRecebimentoUseCase;
import com.bella.backend.domain.compra.port.in.ListarComprasUseCase;
import com.bella.backend.domain.compra.port.in.ReceberCompraUseCase;
import com.bella.backend.domain.compra.port.out.CompraRepositoryPort;
import com.bella.backend.domain.estoque.port.in.ItemEntradaCompra;
import com.bella.backend.domain.estoque.port.in.RegistrarEntradasDeCompraUseCase;
import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.produto.model.StatusProduto;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CompraService implements
        CadastrarCompraUseCase,
        EditarCompraUseCase,
        BuscarCompraPorIdUseCase,
        ListarComprasUseCase,
        ListarComprasPendentesRecebimentoUseCase,
        ConfirmarCompraUseCase,
        ReceberCompraUseCase,
        CancelarCompraUseCase {

    private final CompraRepositoryPort compraRepositoryPort;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;
    private final RegistrarEntradasDeCompraUseCase registrarEntradasDeCompraUseCase;
    private final CadastrarContaPagarUseCase cadastrarContaPagarUseCase;

    public CompraService(CompraRepositoryPort compraRepositoryPort,
                          BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase,
                          BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase,
                          RegistrarEntradasDeCompraUseCase registrarEntradasDeCompraUseCase,
                          CadastrarContaPagarUseCase cadastrarContaPagarUseCase) {
        this.compraRepositoryPort = compraRepositoryPort;
        this.buscarFornecedorPorIdUseCase = buscarFornecedorPorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
        this.registrarEntradasDeCompraUseCase = registrarEntradasDeCompraUseCase;
        this.cadastrarContaPagarUseCase = cadastrarContaPagarUseCase;
    }

    @Override
    public Compra cadastrar(ComandoCompra comando) {
        validarReferencias(comando);

        Compra compra = Compra.nova(
                comando.fornecedorId(),
                comando.previsaoRecebimento(),
                paraItens(comando.itens()),
                comando.descontoGeral(),
                comando.frete(),
                comando.outrasDespesas(),
                comando.condicaoPagamento(),
                comando.observacoes());

        return compraRepositoryPort.salvar(compra);
    }

    @Override
    public Compra editar(UUID id, ComandoCompra comando) {
        Compra compra = buscarPorId(id);
        validarReferencias(comando);

        compra.editar(
                comando.fornecedorId(),
                comando.previsaoRecebimento(),
                paraItens(comando.itens()),
                comando.descontoGeral(),
                comando.frete(),
                comando.outrasDespesas(),
                comando.condicaoPagamento(),
                comando.observacoes());

        return compraRepositoryPort.salvar(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public Compra buscarPorId(UUID id) {
        return compraRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Compra", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> listar(FiltroListagem filtro) {
        return compraRepositoryPort.listar(filtro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPendentesDeRecebimento() {
        return compraRepositoryPort.listar(new FiltroListagem(null, StatusCompra.CONFIRMADA, null, null));
    }

    @Override
    public Compra confirmar(UUID id) {
        Compra compra = buscarPorId(id);
        compra.confirmar();
        return compraRepositoryPort.salvar(compra);
    }

    /**
     * A compra só é salva como RECEBIDA depois que todas as entradas de estoque foram concluídas.
     * Se qualquer entrada falhar, a exceção desfaz a transação inteira: nenhuma movimentação
     * parcial e nenhuma mudança de status são persistidas.
     */
    @Override
    public Compra receber(UUID id, LocalDate dataRecebimento) {
        Compra compra = buscarPorId(id);
        compra.receber(dataRecebimento);

        List<ItemEntradaCompra> entradas = compra.getItens().stream()
                .map(item -> new ItemEntradaCompra(item.getProdutoId(), item.getQuantidade(), item.getCustoUnitario()))
                .toList();
        registrarEntradasDeCompraUseCase.registrarEntradasDeCompra(compra.getId(), entradas);
        gerarContaPagar(compra);

        return compraRepositoryPort.salvar(compra);
    }

    @Override
    public Compra cancelar(UUID id, String motivo) {
        Compra compra = buscarPorId(id);
        compra.cancelar(motivo);
        return compraRepositoryPort.salvar(compra);
    }

    /** O prazo da condição de pagamento conta a partir do recebimento da mercadoria. */
    private void gerarContaPagar(Compra compra) {
        if (compra.getValorTotal().signum() <= 0) {
            return;
        }
        List<ComandoParcelaContaPagar> parcelas = CondicaoPagamentoParcelas
                .gerar(compra.getCondicaoPagamento(), compra.getValorTotal(), compra.getDataRecebimento())
                .stream()
                .map(parcela -> new ComandoParcelaContaPagar(parcela.valor(), parcela.vencimento()))
                .toList();

        cadastrarContaPagarUseCase.cadastrar(new ComandoContaPagar(
                "Compra " + compra.getId().toString().substring(0, 8),
                compra.getFornecedorId(),
                CategoriasDespesaDoSistema.COMPRA_DE_MERCADORIAS,
                compra.getValorTotal(),
                null,
                parcelas,
                compra.getObservacoes(),
                OrigemContaPagar.COMPRA,
                compra.getId()));
    }

    private void validarReferencias(ComandoCompra comando) {
        if (comando.fornecedorId() == null) {
            throw new RegraDeNegocioException("Fornecedor é obrigatório");
        }
        if (buscarFornecedorPorIdUseCase.buscarPorId(comando.fornecedorId()).getStatus()
                != StatusFornecedor.ATIVO) {
            throw new RegraDeNegocioException("Fornecedor inativo não pode ser utilizado em compras");
        }
        if (comando.itens() == null) {
            return;
        }
        for (ComandoItemCompra item : comando.itens()) {
            if (item.produtoId() == null) {
                throw new RegraDeNegocioException("Produto do item é obrigatório");
            }
            if (buscarProdutoPorIdUseCase.buscarPorId(item.produtoId()).getStatus() != StatusProduto.ATIVO) {
                throw new RegraDeNegocioException("Produto inativo não pode ser adicionado a uma compra");
            }
        }
    }

    private List<CompraItem> paraItens(List<ComandoItemCompra> itensComando) {
        if (itensComando == null) {
            return List.of();
        }
        return itensComando.stream()
                .map(item -> CompraItem.novo(item.produtoId(), item.quantidade(), item.custoUnitario(),
                        item.desconto()))
                .toList();
    }
}
