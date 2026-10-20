package com.bella.backend.application.fluxocaixa.usecase;

import com.bella.backend.domain.categoriafluxocaixa.model.StatusCategoriaFluxoCaixa;
import com.bella.backend.domain.categoriafluxocaixa.port.in.BuscarCategoriaFluxoCaixaPorIdUseCase;
import com.bella.backend.domain.fluxocaixa.model.MovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.OrigemMovimentacaoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCaixa;
import com.bella.backend.domain.fluxocaixa.model.ResumoCategoriaCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.BuscarMovimentacaoCaixaPorIdUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.CancelarMovimentacaoAutomaticaCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.CancelarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ComandoLancamentoManualCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ComandoMovimentacaoAutomaticaCaixa;
import com.bella.backend.domain.fluxocaixa.port.in.ConsultarFluxoCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.EditarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.LancarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.ListarMovimentacoesCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.RealizarMovimentacaoAutomaticaCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.RealizarMovimentacaoManualCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.in.RegistrarMovimentacaoAutomaticaCaixaUseCase;
import com.bella.backend.domain.fluxocaixa.port.out.MovimentacaoCaixaRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class FluxoCaixaService implements
        LancarMovimentacaoManualCaixaUseCase,
        EditarMovimentacaoManualCaixaUseCase,
        RealizarMovimentacaoManualCaixaUseCase,
        CancelarMovimentacaoManualCaixaUseCase,
        BuscarMovimentacaoCaixaPorIdUseCase,
        ListarMovimentacoesCaixaUseCase,
        ConsultarFluxoCaixaUseCase,
        RegistrarMovimentacaoAutomaticaCaixaUseCase,
        RealizarMovimentacaoAutomaticaCaixaUseCase,
        CancelarMovimentacaoAutomaticaCaixaUseCase {

    private final MovimentacaoCaixaRepositoryPort movimentacaoCaixaRepositoryPort;
    private final BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase;

    public FluxoCaixaService(MovimentacaoCaixaRepositoryPort movimentacaoCaixaRepositoryPort,
                              BuscarCategoriaFluxoCaixaPorIdUseCase buscarCategoriaFluxoCaixaPorIdUseCase) {
        this.movimentacaoCaixaRepositoryPort = movimentacaoCaixaRepositoryPort;
        this.buscarCategoriaFluxoCaixaPorIdUseCase = buscarCategoriaFluxoCaixaPorIdUseCase;
    }

    @Override
    public MovimentacaoCaixa lancar(ComandoLancamentoManualCaixa comando) {
        validarCategoria(comando.categoriaId());

        MovimentacaoCaixa movimentacao = MovimentacaoCaixa.novaManual(
                comando.tipo(),
                comando.descricao(),
                comando.categoriaId(),
                comando.valor(),
                comando.situacao(),
                comando.dataPrevista(),
                comando.dataEfetiva(),
                comando.observacoes());

        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    @Override
    public MovimentacaoCaixa editar(UUID id, ComandoLancamentoManualCaixa comando) {
        MovimentacaoCaixa movimentacao = buscarPorId(id);
        validarCategoria(comando.categoriaId());

        movimentacao.editarManual(
                comando.tipo(),
                comando.descricao(),
                comando.categoriaId(),
                comando.valor(),
                comando.dataPrevista(),
                comando.observacoes());

        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    @Override
    public MovimentacaoCaixa realizar(UUID id, LocalDate dataEfetiva) {
        MovimentacaoCaixa movimentacao = buscarPorId(id);
        exigirManual(movimentacao);
        movimentacao.realizar(dataEfetiva);
        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    @Override
    public MovimentacaoCaixa cancelar(UUID id, String motivo) {
        MovimentacaoCaixa movimentacao = buscarPorId(id);
        movimentacao.cancelarManual(motivo);
        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimentacaoCaixa buscarPorId(UUID id) {
        return movimentacaoCaixaRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Movimentação de caixa", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoCaixa> listar(FiltroListagem filtro) {
        return movimentacaoCaixaRepositoryPort
                .listar(filtro.tipo(), filtro.categoriaId(), filtro.origem(), filtro.situacao(),
                        filtro.incluirCanceladas())
                .stream()
                .filter(movimentacao -> noPeriodo(movimentacao, filtro.dataInicio(), filtro.dataFim()))
                .sorted(Comparator.comparing(MovimentacaoCaixa::getDataReferencia).reversed()
                        .thenComparing(MovimentacaoCaixa::getCriadoEm, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumoCaixa consultarResumo(LocalDate inicio, LocalDate fim) {
        return ResumoCaixa.de(movimentacoesDoPeriodo(inicio, fim));
    }

    @Override
    @Transactional(readOnly = true)
    public ResumoCaixa consultarSaldoAcumulado(LocalDate ate) {
        return ResumoCaixa.de(movimentacoesDoPeriodo(null, ate == null ? LocalDate.now() : ate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumoCategoriaCaixa> consultarResumoPorCategoria(LocalDate inicio, LocalDate fim) {
        Map<UUID, List<MovimentacaoCaixa>> porCategoria = new HashMap<>();
        for (MovimentacaoCaixa movimentacao : movimentacoesDoPeriodo(inicio, fim)) {
            porCategoria.computeIfAbsent(movimentacao.getCategoriaId(), chave -> new ArrayList<>())
                    .add(movimentacao);
        }
        return porCategoria.entrySet().stream()
                .map(entrada -> new ResumoCategoriaCaixa(entrada.getKey(), ResumoCaixa.de(entrada.getValue())))
                .toList();
    }

    @Override
    public MovimentacaoCaixa registrar(ComandoMovimentacaoAutomaticaCaixa comando) {
        if (comando.origem() != null && comando.origemReferenciaId() != null
                && movimentacaoCaixaRepositoryPort
                .buscarPorOrigemEReferencia(comando.origem(), comando.origemReferenciaId()).isPresent()) {
            throw new RegraDeNegocioException("Já existe uma movimentação de caixa para esta origem");
        }
        if (comando.categoriaId() != null) {
            buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(comando.categoriaId());
        }

        MovimentacaoCaixa movimentacao = MovimentacaoCaixa.novaAutomatica(
                comando.tipo(),
                comando.descricao(),
                comando.categoriaId(),
                comando.valor(),
                comando.situacao(),
                comando.dataPrevista(),
                comando.dataEfetiva(),
                comando.origem(),
                comando.origemReferenciaId(),
                comando.observacoes());

        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    @Override
    public MovimentacaoCaixa realizar(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId,
                                       LocalDate dataEfetiva) {
        MovimentacaoCaixa movimentacao = buscarPorOrigem(origem, origemReferenciaId);
        movimentacao.realizar(dataEfetiva);
        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    @Override
    public MovimentacaoCaixa cancelar(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId, String motivo) {
        MovimentacaoCaixa movimentacao = buscarPorOrigem(origem, origemReferenciaId);
        movimentacao.cancelarPorOrigem(motivo);
        return movimentacaoCaixaRepositoryPort.salvar(movimentacao);
    }

    private List<MovimentacaoCaixa> movimentacoesDoPeriodo(LocalDate inicio, LocalDate fim) {
        return movimentacaoCaixaRepositoryPort.listar(null, null, null, null, false).stream()
                .filter(movimentacao -> noPeriodo(movimentacao, inicio, fim))
                .toList();
    }

    private MovimentacaoCaixa buscarPorOrigem(OrigemMovimentacaoCaixa origem, UUID origemReferenciaId) {
        return movimentacaoCaixaRepositoryPort.buscarPorOrigemEReferencia(origem, origemReferenciaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Movimentação de caixa", origemReferenciaId));
    }

    private void exigirManual(MovimentacaoCaixa movimentacao) {
        if (movimentacao.getOrigem() != OrigemMovimentacaoCaixa.LANCAMENTO_MANUAL) {
            throw new RegraDeNegocioException("Movimentação automática não pode ser alterada manualmente");
        }
    }

    private void validarCategoria(UUID categoriaId) {
        if (categoriaId != null && buscarCategoriaFluxoCaixaPorIdUseCase.buscarPorId(categoriaId)
                .getStatus() != StatusCategoriaFluxoCaixa.ATIVO) {
            throw new RegraDeNegocioException("Categoria inativa não pode ser utilizada");
        }
    }

    private boolean noPeriodo(MovimentacaoCaixa movimentacao, LocalDate inicio, LocalDate fim) {
        LocalDate data = movimentacao.getDataReferencia();
        return (inicio == null || !data.isBefore(inicio)) && (fim == null || !data.isAfter(fim));
    }
}
