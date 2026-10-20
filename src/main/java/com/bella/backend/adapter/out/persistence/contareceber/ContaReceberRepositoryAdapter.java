package com.bella.backend.adapter.out.persistence.contareceber;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.RecebimentoContaReceber;
import com.bella.backend.domain.contareceber.model.ParcelaContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.contareceber.port.out.ContaReceberRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContaReceberRepositoryAdapter implements ContaReceberRepositoryPort {

    private final ContaReceberJpaRepository contaReceberJpaRepository;

    public ContaReceberRepositoryAdapter(ContaReceberJpaRepository contaReceberJpaRepository) {
        this.contaReceberJpaRepository = contaReceberJpaRepository;
    }

    @Override
    public ContaReceber salvar(ContaReceber conta) {
        ContaReceberJpaEntity salvo = contaReceberJpaRepository.save(paraEntidade(conta));
        return paraDominio(salvo);
    }

    @Override
    public Optional<ContaReceber> buscarPorId(UUID id) {
        return contaReceberJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<ContaReceber> listar(UUID clienteId, StatusContaReceber status) {
        return contaReceberJpaRepository.buscarPorFiltro(clienteId, status).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public boolean existePorOrigemEReferencia(OrigemContaReceber origem, UUID origemReferenciaId) {
        return contaReceberJpaRepository.existsByOrigemAndOrigemReferenciaId(origem, origemReferenciaId);
    }

    private ContaReceberJpaEntity paraEntidade(ContaReceber conta) {
        List<ParcelaContaReceberJpaEntity> parcelas = conta.getParcelas().stream()
                .map(parcela -> new ParcelaContaReceberJpaEntity(
                        parcela.getId(), parcela.getNumero(), parcela.getValor(), parcela.getVencimento(),
                        parcela.getStatus(),
                        parcela.getRecebimentos().stream()
                                .map(recebimento -> new RecebimentoContaReceberJpaEntity(
                                        recebimento.getId(), recebimento.getValorRecebido(), recebimento.getDataRecebimento(),
                                        recebimento.getJuros(), recebimento.getMulta(), recebimento.getDesconto(),
                                        recebimento.getObservacao(), recebimento.getCriadoEm()))
                                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new))))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        return new ContaReceberJpaEntity(
                conta.getId(),
                conta.getDescricao(),
                conta.getClienteId(),
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

    private ContaReceber paraDominio(ContaReceberJpaEntity entidade) {
        List<ParcelaContaReceber> parcelas = entidade.getParcelas().stream()
                .map(parcela -> ParcelaContaReceber.existente(
                        parcela.getId(), parcela.getNumero(), parcela.getValor(), parcela.getVencimento(),
                        parcela.getStatus(),
                        parcela.getRecebimentos().stream()
                                .map(recebimento -> RecebimentoContaReceber.existente(
                                        recebimento.getId(), recebimento.getValorRecebido(), recebimento.getDataRecebimento(),
                                        recebimento.getJuros(), recebimento.getMulta(), recebimento.getDesconto(),
                                        recebimento.getObservacao(), recebimento.getCriadoEm()))
                                .toList()))
                .toList();

        return ContaReceber.existente(
                entidade.getId(),
                entidade.getDescricao(),
                entidade.getClienteId(),
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
