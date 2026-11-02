package com.bella.backend.application.venda.usecase;

import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.port.in.CadastrarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.CancelarContaReceberPorOrigemUseCase;
import com.bella.backend.domain.contareceber.port.in.ComandoContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoParcelaContaReceber;
import com.bella.backend.domain.pedido.model.Pedido;
import com.bella.backend.domain.shared.CondicaoPagamentoParcelas;
import com.bella.backend.domain.pedido.model.StatusPedido;
import com.bella.backend.domain.pedido.port.in.BuscarPedidoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.Venda;
import com.bella.backend.domain.venda.model.VendaItem;
import com.bella.backend.domain.venda.port.in.BuscarVendaPorIdUseCase;
import com.bella.backend.domain.venda.port.in.CancelarVendaUseCase;
import com.bella.backend.domain.venda.port.in.ConcluirVendaUseCase;
import com.bella.backend.domain.venda.port.in.ListarVendasUseCase;
import com.bella.backend.domain.venda.port.out.VendaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VendaService implements
        ConcluirVendaUseCase,
        BuscarVendaPorIdUseCase,
        ListarVendasUseCase,
        CancelarVendaUseCase {

    private final VendaRepositoryPort vendaRepositoryPort;
    private final BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase;
    private final CadastrarContaReceberUseCase cadastrarContaReceberUseCase;
    private final CancelarContaReceberPorOrigemUseCase cancelarContaReceberPorOrigemUseCase;

    public VendaService(VendaRepositoryPort vendaRepositoryPort, BuscarPedidoPorIdUseCase buscarPedidoPorIdUseCase,
                        CadastrarContaReceberUseCase cadastrarContaReceberUseCase,
                        CancelarContaReceberPorOrigemUseCase cancelarContaReceberPorOrigemUseCase) {
        this.vendaRepositoryPort = vendaRepositoryPort;
        this.buscarPedidoPorIdUseCase = buscarPedidoPorIdUseCase;
        this.cadastrarContaReceberUseCase = cadastrarContaReceberUseCase;
        this.cancelarContaReceberPorOrigemUseCase = cancelarContaReceberPorOrigemUseCase;
    }

    @Override
    public Venda concluir(UUID pedidoId, FormaPagamento formaPagamento) {
        Pedido pedido = buscarPedidoPorIdUseCase.buscarPorId(pedidoId);

        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraDeNegocioException("Só é possível concluir a venda de um pedido entregue");
        }
        if (vendaRepositoryPort.existePorPedidoId(pedidoId)) {
            throw new RegraDeNegocioException("Este pedido já foi convertido em venda");
        }

        List<VendaItem> itens = pedido.getItens().stream()
                .map(item -> VendaItem.novo(item.getProdutoId(), item.getQuantidade(), item.getValorUnitario()))
                .toList();

        Venda venda = Venda.concluir(
                pedido.getId(),
                pedido.getClienteId(),
                itens,
                pedido.getPercentualDesconto(),
                pedido.getValorFrete(),
                formaPagamento,
                pedido.getCondicaoPagamento());

        Venda salva = vendaRepositoryPort.salvar(venda);
        gerarContaReceber(salva);
        return salva;
    }

    @Override
    @Transactional(readOnly = true)
    public Venda buscarPorId(UUID id) {
        return vendaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Venda", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venda> listar(FiltroListagem filtro) {
        return vendaRepositoryPort.listar(filtro);
    }

    @Override
    public Venda cancelar(UUID id) {
        Venda venda = buscarPorId(id);
        venda.cancelar();
        cancelarContaReceberPorOrigemUseCase.cancelarPorOrigem(OrigemContaReceber.VENDA, venda.getId(),
                "Venda cancelada");
        return vendaRepositoryPort.salvar(venda);
    }

    /** O prazo da condição de pagamento conta a partir da conclusão da venda. */
    private void gerarContaReceber(Venda venda) {
        if (venda.getValorTotal().signum() <= 0) {
            return;
        }
        List<ComandoParcelaContaReceber> parcelas = CondicaoPagamentoParcelas
                .gerar(venda.getCondicaoPagamento(), venda.getValorTotal(), LocalDate.now())
                .stream()
                .map(parcela -> new ComandoParcelaContaReceber(parcela.valor(), parcela.vencimento()))
                .toList();

        cadastrarContaReceberUseCase.cadastrar(new ComandoContaReceber(
                "Venda " + venda.getId().toString().substring(0, 8),
                venda.getClienteId(),
                venda.getValorTotal(),
                null,
                parcelas,
                null,
                OrigemContaReceber.VENDA,
                venda.getId()));
    }
}
