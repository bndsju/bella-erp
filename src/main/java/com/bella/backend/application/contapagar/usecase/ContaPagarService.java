package com.bella.backend.application.contapagar.usecase;

import com.bella.backend.domain.categoriadespesa.model.StatusCategoriaDespesa;
import com.bella.backend.domain.categoriadespesa.port.in.BuscarCategoriaDespesaPorIdUseCase;
import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.DadosParcela;
import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.ParcelaContaPagar;
import com.bella.backend.domain.contapagar.model.SituacaoContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.contapagar.port.in.BuscarContaPagarPorIdUseCase;
import com.bella.backend.domain.contapagar.port.in.CadastrarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.CancelarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.ComandoContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoDadosBasicosContaPagar;
import com.bella.backend.domain.contapagar.port.in.ComandoPagamentoContaPagar;
import com.bella.backend.domain.contapagar.port.in.EditarContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.EditarDadosBasicosContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.ListarContasPagarUseCase;
import com.bella.backend.domain.contapagar.port.in.RegistrarPagamentoContaPagarUseCase;
import com.bella.backend.domain.contapagar.port.out.ContaPagarRepositoryPort;
import com.bella.backend.domain.fornecedor.model.StatusFornecedor;
import com.bella.backend.domain.fornecedor.port.in.BuscarFornecedorPorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContaPagarService implements
        CadastrarContaPagarUseCase,
        EditarContaPagarUseCase,
        EditarDadosBasicosContaPagarUseCase,
        BuscarContaPagarPorIdUseCase,
        ListarContasPagarUseCase,
        RegistrarPagamentoContaPagarUseCase,
        CancelarContaPagarUseCase {

    private final ContaPagarRepositoryPort contaPagarRepositoryPort;
    private final BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;
    private final BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase;

    public ContaPagarService(ContaPagarRepositoryPort contaPagarRepositoryPort,
                              BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase,
                              BuscarCategoriaDespesaPorIdUseCase buscarCategoriaDespesaPorIdUseCase) {
        this.contaPagarRepositoryPort = contaPagarRepositoryPort;
        this.buscarFornecedorPorIdUseCase = buscarFornecedorPorIdUseCase;
        this.buscarCategoriaDespesaPorIdUseCase = buscarCategoriaDespesaPorIdUseCase;
    }

    @Override
    public ContaPagar cadastrar(ComandoContaPagar comando) {
        validarReferencias(comando.fornecedorId(), comando.categoriaDespesaId());

        OrigemContaPagar origem = comando.origem() == null ? OrigemContaPagar.MANUAL : comando.origem();
        if (origem != OrigemContaPagar.MANUAL && comando.origemReferenciaId() != null
                && contaPagarRepositoryPort.existePorOrigemEReferencia(origem, comando.origemReferenciaId())) {
            throw new RegraDeNegocioException("Já existe uma conta a pagar para esta origem");
        }

        ContaPagar conta = ContaPagar.nova(
                comando.descricao(),
                comando.fornecedorId(),
                comando.categoriaDespesaId(),
                comando.valorTotal(),
                comando.vencimento(),
                paraDadosParcelas(comando),
                comando.observacoes(),
                origem,
                comando.origemReferenciaId());

        return contaPagarRepositoryPort.salvar(conta);
    }

    @Override
    public ContaPagar editar(UUID id, ComandoContaPagar comando) {
        ContaPagar conta = buscarPorId(id);
        validarReferencias(comando.fornecedorId(), comando.categoriaDespesaId());

        conta.editar(
                comando.descricao(),
                comando.fornecedorId(),
                comando.categoriaDespesaId(),
                comando.valorTotal(),
                comando.vencimento(),
                paraDadosParcelas(comando),
                comando.observacoes());

        return contaPagarRepositoryPort.salvar(conta);
    }

    @Override
    public ContaPagar editarDadosBasicos(UUID id, ComandoDadosBasicosContaPagar comando) {
        ContaPagar conta = buscarPorId(id);
        validarReferencias(comando.fornecedorId(), comando.categoriaDespesaId());

        conta.editarDadosBasicos(comando.descricao(), comando.fornecedorId(), comando.categoriaDespesaId(),
                comando.observacoes());

        return contaPagarRepositoryPort.salvar(conta);
    }

    @Override
    @Transactional(readOnly = true)
    public ContaPagar buscarPorId(UUID id) {
        return contaPagarRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Conta a pagar", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContaPagar> listar(FiltroListagem filtro) {
        LocalDate hoje = LocalDate.now();
        return contaPagarRepositoryPort.listar(filtro.fornecedorId(), filtro.categoriaDespesaId(), filtro.status())
                .stream()
                .filter(conta -> venceNoPeriodo(conta, filtro.vencimentoInicio(), filtro.vencimentoFim()))
                .filter(conta -> naSituacao(conta, filtro.situacao(), hoje))
                .toList();
    }

    @Override
    public ContaPagar registrarPagamento(UUID contaId, ComandoPagamentoContaPagar comando) {
        ContaPagar conta = buscarPorId(contaId);

        conta.registrarPagamento(
                comando.parcelaId(),
                comando.valorPago(),
                comando.dataPagamento() == null ? LocalDate.now() : comando.dataPagamento(),
                comando.juros(),
                comando.multa(),
                comando.desconto(),
                comando.observacao());

        return contaPagarRepositoryPort.salvar(conta);
    }

    @Override
    public ContaPagar cancelar(UUID id, String motivo) {
        ContaPagar conta = buscarPorId(id);
        conta.cancelar(motivo);
        return contaPagarRepositoryPort.salvar(conta);
    }

    private void validarReferencias(UUID fornecedorId, UUID categoriaDespesaId) {
        if (fornecedorId != null
                && buscarFornecedorPorIdUseCase.buscarPorId(fornecedorId).getStatus() != StatusFornecedor.ATIVO) {
            throw new RegraDeNegocioException("Fornecedor inativo não pode ser utilizado em contas a pagar");
        }
        if (categoriaDespesaId != null && buscarCategoriaDespesaPorIdUseCase.buscarPorId(categoriaDespesaId)
                .getStatus() != StatusCategoriaDespesa.ATIVO) {
            throw new RegraDeNegocioException("Categoria de despesa inativa não pode ser utilizada");
        }
    }

    private List<DadosParcela> paraDadosParcelas(ComandoContaPagar comando) {
        if (comando.parcelas() == null) {
            return List.of();
        }
        return comando.parcelas().stream()
                .map(parcela -> new DadosParcela(parcela.valor(), parcela.vencimento()))
                .toList();
    }

    private boolean venceNoPeriodo(ContaPagar conta, LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            return true;
        }
        return conta.getParcelas().stream().map(ParcelaContaPagar::getVencimento)
                .anyMatch(vencimento -> (inicio == null || !vencimento.isBefore(inicio))
                        && (fim == null || !vencimento.isAfter(fim)));
    }

    private boolean naSituacao(ContaPagar conta, SituacaoContaPagar situacao, LocalDate hoje) {
        if (situacao == null) {
            return true;
        }
        return switch (situacao) {
            case EM_ABERTO -> conta.estaEmAberto();
            case PAGAS -> conta.getStatus() == StatusContaPagar.PAGA;
            case VENCIDAS -> conta.estaVencida(hoje);
        };
    }
}
