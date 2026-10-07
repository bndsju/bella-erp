package com.bella.backend.adapter.out.persistence.contapagar;

import com.bella.backend.domain.contapagar.model.StatusParcela;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "parcelas_conta_pagar", schema = "bella")
public class ParcelaContaPagarJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusParcela status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "parcela_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<PagamentoContaPagarJpaEntity> pagamentos = new ArrayList<>();

    protected ParcelaContaPagarJpaEntity() {
    }

    public ParcelaContaPagarJpaEntity(UUID id, int numero, BigDecimal valor, LocalDate vencimento,
                                       StatusParcela status, List<PagamentoContaPagarJpaEntity> pagamentos) {
        this.id = id;
        this.numero = numero;
        this.valor = valor;
        this.vencimento = vencimento;
        this.status = status;
        this.pagamentos = pagamentos;
    }

    public UUID getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public StatusParcela getStatus() {
        return status;
    }

    public List<PagamentoContaPagarJpaEntity> getPagamentos() {
        return pagamentos;
    }
}
