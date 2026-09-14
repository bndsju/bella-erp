package com.bella.backend.application.orcamento.usecase;

import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.orcamento.model.Orcamento;
import com.bella.backend.domain.orcamento.model.OrcamentoItem;
import com.bella.backend.domain.orcamento.port.in.AprovarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.BuscarOrcamentoPorIdUseCase;
import com.bella.backend.domain.orcamento.port.in.CadastrarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.CancelarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.ComandoItemOrcamento;
import com.bella.backend.domain.orcamento.port.in.ComandoOrcamento;
import com.bella.backend.domain.orcamento.port.in.EditarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.EnviarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.in.ListarOrcamentosUseCase;
import com.bella.backend.domain.orcamento.port.in.RecusarOrcamentoUseCase;
import com.bella.backend.domain.orcamento.port.out.OrcamentoRepositoryPort;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrcamentoService implements
        CadastrarOrcamentoUseCase,
        EditarOrcamentoUseCase,
        BuscarOrcamentoPorIdUseCase,
        ListarOrcamentosUseCase,
        EnviarOrcamentoUseCase,
        AprovarOrcamentoUseCase,
        RecusarOrcamentoUseCase,
        CancelarOrcamentoUseCase {

    private final OrcamentoRepositoryPort orcamentoRepositoryPort;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase;

    public OrcamentoService(OrcamentoRepositoryPort orcamentoRepositoryPort,
                             BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                             BuscarProdutoPorIdUseCase buscarProdutoPorIdUseCase) {
        this.orcamentoRepositoryPort = orcamentoRepositoryPort;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.buscarProdutoPorIdUseCase = buscarProdutoPorIdUseCase;
    }

    @Override
    public Orcamento cadastrar(ComandoOrcamento comando) {
        validarReferencias(comando);

        Orcamento orcamento = Orcamento.novo(
                comando.clienteId(),
                paraItens(comando.itens()),
                comando.percentualDesconto(),
                comando.valorFrete(),
                comando.prazoValidade(),
                comando.condicaoPagamento(),
                comando.observacoes());

        return orcamentoRepositoryPort.salvar(orcamento);
    }

    @Override
    public Orcamento editar(UUID id, ComandoOrcamento comando) {
        Orcamento orcamento = buscarEAtualizarExpiracao(id);
        validarReferencias(comando);

        orcamento.editar(
                comando.clienteId(),
                paraItens(comando.itens()),
                comando.percentualDesconto(),
                comando.valorFrete(),
                comando.prazoValidade(),
                comando.condicaoPagamento(),
                comando.observacoes());

        return orcamentoRepositoryPort.salvar(orcamento);
    }

    @Override
    @Transactional(readOnly = true)
    public Orcamento buscarPorId(UUID id) {
        return buscarEAtualizarExpiracao(id);
    }

    @Override
    public List<Orcamento> listar(FiltroListagem filtro) {
        return orcamentoRepositoryPort.listar(filtro).stream()
                .map(this::salvarSeExpirou)
                .toList();
    }

    @Override
    public Orcamento enviar(UUID id) {
        Orcamento orcamento = buscarEAtualizarExpiracao(id);
        orcamento.enviar();
        return orcamentoRepositoryPort.salvar(orcamento);
    }

    @Override
    public Orcamento aprovar(UUID id) {
        Orcamento orcamento = buscarEAtualizarExpiracao(id);
        orcamento.aprovar();
        return orcamentoRepositoryPort.salvar(orcamento);
    }

    @Override
    public Orcamento recusar(UUID id) {
        Orcamento orcamento = buscarEAtualizarExpiracao(id);
        orcamento.recusar();
        return orcamentoRepositoryPort.salvar(orcamento);
    }

    @Override
    public Orcamento cancelar(UUID id) {
        Orcamento orcamento = buscarEAtualizarExpiracao(id);
        orcamento.cancelar();
        return orcamentoRepositoryPort.salvar(orcamento);
    }

    private Orcamento buscarEAtualizarExpiracao(UUID id) {
        Orcamento orcamento = orcamentoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Orçamento", id));
        return salvarSeExpirou(orcamento);
    }

    private Orcamento salvarSeExpirou(Orcamento orcamento) {
        if (orcamento.expirarSeNecessario()) {
            return orcamentoRepositoryPort.salvar(orcamento);
        }
        return orcamento;
    }

    private void validarReferencias(ComandoOrcamento comando) {
        buscarClientePorIdUseCase.buscarPorId(comando.clienteId());
        for (ComandoItemOrcamento item : comando.itens()) {
            buscarProdutoPorIdUseCase.buscarPorId(item.produtoId());
        }
    }

    private List<OrcamentoItem> paraItens(List<ComandoItemOrcamento> itensComando) {
        return itensComando.stream()
                .map(item -> OrcamentoItem.novo(item.produtoId(), item.quantidade(), item.valorUnitario()))
                .toList();
    }
}
