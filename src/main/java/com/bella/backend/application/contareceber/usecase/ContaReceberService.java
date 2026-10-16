package com.bella.backend.application.contareceber.usecase;

import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.DadosParcela;
import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.ParcelaContaReceber;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.contareceber.port.in.BuscarContaReceberPorIdUseCase;
import com.bella.backend.domain.contareceber.port.in.CadastrarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.CancelarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.ComandoContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoDadosBasicosContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoRecebimentoContaReceber;
import com.bella.backend.domain.contareceber.port.in.EditarContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.EditarDadosBasicosContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.ListarContasReceberUseCase;
import com.bella.backend.domain.contareceber.port.in.RegistrarRecebimentoContaReceberUseCase;
import com.bella.backend.domain.contareceber.port.out.ContaReceberRepositoryPort;
import com.bella.backend.domain.cliente.model.StatusCliente;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContaReceberService implements
        CadastrarContaReceberUseCase,
        EditarContaReceberUseCase,
        EditarDadosBasicosContaReceberUseCase,
        BuscarContaReceberPorIdUseCase,
        ListarContasReceberUseCase,
        RegistrarRecebimentoContaReceberUseCase,
        CancelarContaReceberUseCase {

    private final ContaReceberRepositoryPort contaReceberRepositoryPort;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    public ContaReceberService(ContaReceberRepositoryPort contaReceberRepositoryPort,
                              BuscarClientePorIdUseCase buscarClientePorIdUseCase) {
        this.contaReceberRepositoryPort = contaReceberRepositoryPort;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
    }

    @Override
    public ContaReceber cadastrar(ComandoContaReceber comando) {
        validarReferencias(comando.clienteId());

        OrigemContaReceber origem = comando.origem() == null ? OrigemContaReceber.MANUAL : comando.origem();
        if (origem != OrigemContaReceber.MANUAL && comando.origemReferenciaId() != null
                && contaReceberRepositoryPort.existePorOrigemEReferencia(origem, comando.origemReferenciaId())) {
            throw new RegraDeNegocioException("Já existe uma conta a receber para esta origem");
        }

        ContaReceber conta = ContaReceber.nova(
                comando.descricao(),
                comando.clienteId(),
                comando.valorTotal(),
                comando.vencimento(),
                paraDadosParcelas(comando),
                comando.observacoes(),
                origem,
                comando.origemReferenciaId());

        return contaReceberRepositoryPort.salvar(conta);
    }

    @Override
    public ContaReceber editar(UUID id, ComandoContaReceber comando) {
        ContaReceber conta = buscarPorId(id);
        validarReferencias(comando.clienteId());

        conta.editar(
                comando.descricao(),
                comando.clienteId(),
                comando.valorTotal(),
                comando.vencimento(),
                paraDadosParcelas(comando),
                comando.observacoes());

        return contaReceberRepositoryPort.salvar(conta);
    }

    @Override
    public ContaReceber editarDadosBasicos(UUID id, ComandoDadosBasicosContaReceber comando) {
        ContaReceber conta = buscarPorId(id);
        validarReferencias(comando.clienteId());

        conta.editarDadosBasicos(comando.descricao(), comando.clienteId(),
                comando.observacoes());

        return contaReceberRepositoryPort.salvar(conta);
    }

    @Override
    @Transactional(readOnly = true)
    public ContaReceber buscarPorId(UUID id) {
        return contaReceberRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Conta a receber", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContaReceber> listar(FiltroListagem filtro) {
        LocalDate hoje = LocalDate.now();
        return contaReceberRepositoryPort.listar(filtro.clienteId(), filtro.status())
                .stream()
                .filter(conta -> venceNoPeriodo(conta, filtro.vencimentoInicio(), filtro.vencimentoFim()))
                .filter(conta -> naSituacao(conta, filtro.situacao(), hoje))
                .toList();
    }

    @Override
    public ContaReceber registrarRecebimento(UUID contaId, ComandoRecebimentoContaReceber comando) {
        ContaReceber conta = buscarPorId(contaId);

        conta.registrarRecebimento(
                comando.parcelaId(),
                comando.valorRecebido(),
                comando.dataRecebimento() == null ? LocalDate.now() : comando.dataRecebimento(),
                comando.juros(),
                comando.multa(),
                comando.desconto(),
                comando.observacao());

        return contaReceberRepositoryPort.salvar(conta);
    }

    @Override
    public ContaReceber cancelar(UUID id, String motivo) {
        ContaReceber conta = buscarPorId(id);
        conta.cancelar(motivo);
        return contaReceberRepositoryPort.salvar(conta);
    }

    private void validarReferencias(UUID clienteId) {
        if (clienteId != null
                && buscarClientePorIdUseCase.buscarPorId(clienteId).getStatus() != StatusCliente.ATIVO) {
            throw new RegraDeNegocioException("Cliente inativo não pode ser utilizado em contas a receber");
        }
    }

    private List<DadosParcela> paraDadosParcelas(ComandoContaReceber comando) {
        if (comando.parcelas() == null) {
            return List.of();
        }
        return comando.parcelas().stream()
                .map(parcela -> new DadosParcela(parcela.valor(), parcela.vencimento()))
                .toList();
    }

    private boolean venceNoPeriodo(ContaReceber conta, LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) {
            return true;
        }
        return conta.getParcelas().stream().map(ParcelaContaReceber::getVencimento)
                .anyMatch(vencimento -> (inicio == null || !vencimento.isBefore(inicio))
                        && (fim == null || !vencimento.isAfter(fim)));
    }

    private boolean naSituacao(ContaReceber conta, SituacaoContaReceber situacao, LocalDate hoje) {
        if (situacao == null) {
            return true;
        }
        return switch (situacao) {
            case EM_ABERTO -> conta.estaEmAberto();
            case RECEBIDAS -> conta.getStatus() == StatusContaReceber.RECEBIDA;
            case VENCIDAS -> conta.estaVencida(hoje);
        };
    }
}
