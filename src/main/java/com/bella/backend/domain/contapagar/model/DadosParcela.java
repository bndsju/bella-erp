package com.bella.backend.domain.contapagar.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados de entrada de uma parcela (valor e vencimento), antes de virarem uma {@link ParcelaContaPagar}. */
public record DadosParcela(BigDecimal valor, LocalDate vencimento) {
}
