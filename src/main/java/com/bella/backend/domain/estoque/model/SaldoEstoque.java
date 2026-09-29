package com.bella.backend.domain.estoque.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Saldo de estoque de um produto. Nunca é alterado diretamente: toda variação passa por um
 * destes métodos, que também é quem valida os invariantes do módulo (físico e reservado nunca
 * negativos, reservado nunca maior que o físico). O histórico de cada variação é responsabilidade
 * de quem orquestra a operação (camada de aplicação), que registra a {@link MovimentacaoEstoque}
 * correspondente na mesma transação.
 */
public class SaldoEstoque {

    private final UUID produtoId;
    private BigDecimal estoqueFisico;
    private BigDecimal quantidadeReservada;
    private Instant atualizadoEm;

    private SaldoEstoque(UUID produtoId, BigDecimal estoqueFisico, BigDecimal quantidadeReservada,
                          Instant atualizadoEm) {
        this.produtoId = produtoId;
        this.estoqueFisico = estoqueFisico;
        this.quantidadeReservada = quantidadeReservada;
        this.atualizadoEm = atualizadoEm;
    }

    public static SaldoEstoque novo(UUID produtoId) {
        Instant agora = Instant.now();
        return new SaldoEstoque(produtoId, BigDecimal.ZERO, BigDecimal.ZERO, agora);
    }

    public static SaldoEstoque existente(UUID produtoId, BigDecimal estoqueFisico, BigDecimal quantidadeReservada,
                                          Instant atualizadoEm) {
        return new SaldoEstoque(produtoId, estoqueFisico, quantidadeReservada, atualizadoEm);
    }

    public void registrarEntrada(BigDecimal quantidade) {
        estoqueFisico = estoqueFisico.add(validarQuantidade(quantidade));
        atualizadoEm = Instant.now();
    }

    /**
     * Usada tanto pela saída manual quanto pelo ajuste de saída. A validação é contra a
     * quantidade disponível (físico - reservado), não apenas contra o físico, para preservar
     * o invariante de que a quantidade reservada nunca pode superar o estoque físico.
     */
    public void registrarSaida(BigDecimal quantidade) {
        validarQuantidade(quantidade);
        if (quantidade.compareTo(getDisponivel()) > 0) {
            throw new RegraDeNegocioException("Estoque disponível insuficiente para a saída");
        }
        estoqueFisico = estoqueFisico.subtract(quantidade);
        atualizadoEm = Instant.now();
    }

    public void reservar(BigDecimal quantidade) {
        validarQuantidade(quantidade);
        if (quantidade.compareTo(getDisponivel()) > 0) {
            throw new RegraDeNegocioException("Quantidade reservada não pode ser superior ao estoque disponível");
        }
        quantidadeReservada = quantidadeReservada.add(quantidade);
        atualizadoEm = Instant.now();
    }

    public void liberarReserva(BigDecimal quantidade) {
        validarQuantidade(quantidade);
        if (quantidade.compareTo(quantidadeReservada) > 0) {
            throw new RegraDeNegocioException("Quantidade a liberar não pode ser superior à quantidade reservada");
        }
        quantidadeReservada = quantidadeReservada.subtract(quantidade);
        atualizadoEm = Instant.now();
    }

    /**
     * Converte uma reserva em saída definitiva (pedido entregue): reduz reservado e físico na
     * mesma quantidade, sem passar pela validação de {@link #registrarSaida}, já que a
     * quantidade em questão está, por definição, reservada — não apenas disponível.
     */
    public void consumirReserva(BigDecimal quantidade) {
        validarQuantidade(quantidade);
        if (quantidade.compareTo(quantidadeReservada) > 0) {
            throw new RegraDeNegocioException("Quantidade a consumir não pode ser superior à quantidade reservada");
        }
        quantidadeReservada = quantidadeReservada.subtract(quantidade);
        estoqueFisico = estoqueFisico.subtract(quantidade);
        atualizadoEm = Instant.now();
    }

    public BigDecimal getDisponivel() {
        return estoqueFisico.subtract(quantidadeReservada);
    }

    private static BigDecimal validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraDeNegocioException("Quantidade da movimentação deve ser maior que zero");
        }
        return quantidade;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public BigDecimal getEstoqueFisico() {
        return estoqueFisico;
    }

    public BigDecimal getQuantidadeReservada() {
        return quantidadeReservada;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
