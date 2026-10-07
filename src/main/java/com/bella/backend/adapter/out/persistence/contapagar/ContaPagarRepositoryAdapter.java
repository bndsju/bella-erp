package com.bella.backend.adapter.out.persistence.contapagar;

import com.bella.backend.domain.contapagar.model.ContaPagar;
import com.bella.backend.domain.contapagar.model.OrigemContaPagar;
import com.bella.backend.domain.contapagar.model.PagamentoContaPagar;
import com.bella.backend.domain.contapagar.model.ParcelaContaPagar;
import com.bella.backend.domain.contapagar.model.StatusContaPagar;
import com.bella.backend.domain.contapagar.port.out.ContaPagarRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContaPagarRepositoryAdapter implements ContaPagarRepositoryPort {

    private final ContaPagarJpaRepository contaPagarJpaRepository;

    public ContaPagarRepositoryAdapter(ContaPagarJpaRepository contaPagarJpaRepository) {
        this.contaPagarJpaRepository = contaPagarJpaRepository;
    }

    @Override
    public ContaPagar salvar(ContaPagar conta) {
        ContaPagarJpaEntity salvo = contaPagarJpaRepository.save(paraEntidade(conta));
        return paraDominio(salvo);
    }

    @Override
    public Optional<ContaPagar> buscarPorId(UUID id) {
        return contaPagarJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<ContaPagar> listar(UUID fornecedorId, UUID categoriaDespesaId, StatusContaPagar status) {
        return contaPagarJpaRepository.buscarPorFiltro(fornecedorId, categoriaDespesaId, status).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorOrigemEReferencia(OrigemContaPagar origem, UUID origemReferenciaId) {
        return contaPagarJpaRepository.existsByOrigemAndOrigemReferenciaId(origem, origemReferenciaId);
    }

    private ContaPagarJpaEntity paraEntidade(ContaPagar conta) {
        List<ParcelaContaPagarJpaEntity> parcelas = conta.getParcelas().stream()
                .map(parcela -> new ParcelaContaPagarJpaEntity(
                        parcela.getId(), parcela.getNumero(), parcela.getValor(), parcela.getVencimento(),
                        parcela.getStatus(),
                        parcela.getPagamentos().stream()
                                .map(pagamento -> new PagamentoContaPagarJpaEntity(
                                        pagamento.getId(), pagamento.getValorPago(), pagamento.getDataPagamento(),
                                        pagamento.getJuros(), pagamento.getMulta(), pagamento.getDesconto(),
                                        pagamento.getObservacao(), pagamento.getCriadoEm()))
                                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new))))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        return new ContaPagarJpaEntity(
                conta.getId(),
                conta.getDescricao(),
                conta.getFornecedorId(),
                conta.getCategoriaDespesaId(),
                conta.getValorTotal(),
                conta.getDataLancamento(),
                conta.getObservacoes(),
                conta.getOrigem(),
                conta.getOrigemReferenciaId(),
                conta.getStatus(),
                parcelas,
                conta.getMotivoCancelamento(),
                conta.getCanceladaEm(),
                conta.getCriadoEm(),
                conta.getAtualizadoEm());
    }

    private ContaPagar paraDominio(ContaPagarJpaEntity entidade) {
        List<ParcelaContaPagar> parcelas = entidade.getParcelas().stream()
                .map(parcela -> ParcelaContaPagar.existente(
                        parcela.getId(), parcela.getNumero(), parcela.getValor(), parcela.getVencimento(),
                        parcela.getStatus(),
                        parcela.getPagamentos().stream()
                                .map(pagamento -> PagamentoContaPagar.existente(
                                        pagamento.getId(), pagamento.getValorPago(), pagamento.getDataPagamento(),
                                        pagamento.getJuros(), pagamento.getMulta(), pagamento.getDesconto(),
                                        pagamento.getObservacao(), pagamento.getCriadoEm()))
                                .toList()))
                .toList();

        return ContaPagar.existente(
                entidade.getId(),
                entidade.getDescricao(),
                entidade.getFornecedorId(),
                entidade.getCategoriaDespesaId(),
                entidade.getValorTotal(),
                entidade.getDataLancamento(),
                entidade.getObservacoes(),
                entidade.getOrigem(),
                entidade.getOrigemReferenciaId(),
                entidade.getStatus(),
                parcelas,
                entidade.getMotivoCancelamento(),
                entidade.getCanceladaEm(),
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }
}
