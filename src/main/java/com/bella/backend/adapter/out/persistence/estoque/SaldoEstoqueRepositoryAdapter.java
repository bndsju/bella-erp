package com.bella.backend.adapter.out.persistence.estoque;

import com.bella.backend.domain.estoque.model.SaldoEstoque;
import com.bella.backend.domain.estoque.port.out.SaldoEstoqueRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SaldoEstoqueRepositoryAdapter implements SaldoEstoqueRepositoryPort {

    private final SaldoEstoqueJpaRepository saldoEstoqueJpaRepository;

    public SaldoEstoqueRepositoryAdapter(SaldoEstoqueJpaRepository saldoEstoqueJpaRepository) {
        this.saldoEstoqueJpaRepository = saldoEstoqueJpaRepository;
    }

    @Override
    public SaldoEstoque salvar(SaldoEstoque saldo) {
        SaldoEstoqueJpaEntity salvo = saldoEstoqueJpaRepository.save(paraEntidade(saldo));
        return paraDominio(salvo);
    }

    @Override
    public Optional<SaldoEstoque> buscarPorProdutoId(UUID produtoId) {
        return saldoEstoqueJpaRepository.findById(produtoId).map(this::paraDominio);
    }

    @Override
    public List<SaldoEstoque> listarTodos() {
        return saldoEstoqueJpaRepository.findAll().stream().map(this::paraDominio).toList();
    }

    private SaldoEstoqueJpaEntity paraEntidade(SaldoEstoque saldo) {
        return new SaldoEstoqueJpaEntity(saldo.getProdutoId(), saldo.getEstoqueFisico(),
                saldo.getQuantidadeReservada(), saldo.getAtualizadoEm());
    }

    private SaldoEstoque paraDominio(SaldoEstoqueJpaEntity entidade) {
        return SaldoEstoque.existente(entidade.getProdutoId(), entidade.getEstoqueFisico(),
                entidade.getQuantidadeReservada(), entidade.getAtualizadoEm());
    }
}
