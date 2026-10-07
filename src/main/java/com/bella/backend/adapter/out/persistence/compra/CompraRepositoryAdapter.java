package com.bella.backend.adapter.out.persistence.compra;

import com.bella.backend.domain.compra.model.Compra;
import com.bella.backend.domain.compra.model.CompraItem;
import com.bella.backend.domain.compra.port.in.ListarComprasUseCase.FiltroListagem;
import com.bella.backend.domain.compra.port.out.CompraRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CompraRepositoryAdapter implements CompraRepositoryPort {

    private final CompraJpaRepository compraJpaRepository;

    public CompraRepositoryAdapter(CompraJpaRepository compraJpaRepository) {
        this.compraJpaRepository = compraJpaRepository;
    }

    @Override
    public Compra salvar(Compra compra) {
        CompraJpaEntity salvo = compraJpaRepository.save(paraEntidade(compra));
        return paraDominio(salvo);
    }

    @Override
    public Optional<Compra> buscarPorId(UUID id) {
        return compraJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Compra> listar(FiltroListagem filtro) {
        return compraJpaRepository
                .buscarPorFiltro(filtro.fornecedorId(), filtro.status(), filtro.dataInicio(), filtro.dataFim())
                .stream()
                .map(this::paraDominio)
                .toList();
    }

    private CompraJpaEntity paraEntidade(Compra compra) {
        List<CompraItemJpaEntity> itens = compra.getItens().stream()
                .map(item -> new CompraItemJpaEntity(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getCustoUnitario(),
                        item.getDesconto(), item.getValorTotal()))
                .toList();

        return new CompraJpaEntity(
                compra.getId(),
                compra.getFornecedorId(),
                compra.getDataCompra(),
                compra.getPrevisaoRecebimento(),
                compra.getDataRecebimento(),
                itens,
                compra.getDescontoGeral(),
                compra.getFrete(),
                compra.getOutrasDespesas(),
                compra.getValorProdutos(),
                compra.getDescontoTotal(),
                compra.getValorTotal(),
                compra.getCondicaoPagamento(),
                compra.getObservacoes(),
                compra.getStatus(),
                compra.getMotivoCancelamento(),
                compra.getConfirmadaEm(),
                compra.getCanceladaEm(),
                compra.getCriadoEm(),
                compra.getAtualizadoEm());
    }

    private Compra paraDominio(CompraJpaEntity entidade) {
        List<CompraItem> itens = entidade.getItens().stream()
                .map(item -> CompraItem.existente(
                        item.getId(), item.getProdutoId(), item.getQuantidade(), item.getCustoUnitario(),
                        item.getDesconto()))
                .toList();

        return Compra.existente(
                entidade.getId(),
                entidade.getFornecedorId(),
                entidade.getDataCompra(),
                entidade.getPrevisaoRecebimento(),
                entidade.getDataRecebimento(),
                itens,
                entidade.getDescontoGeral(),
                entidade.getFrete(),
                entidade.getOutrasDespesas(),
                entidade.getCondicaoPagamento(),
                entidade.getObservacoes(),
                entidade.getStatus(),
                entidade.getMotivoCancelamento(),
                entidade.getConfirmadaEm(),
                entidade.getCanceladaEm(),
                entidade.getCriadoEm(),
                entidade.getAtualizadoEm());
    }
}
