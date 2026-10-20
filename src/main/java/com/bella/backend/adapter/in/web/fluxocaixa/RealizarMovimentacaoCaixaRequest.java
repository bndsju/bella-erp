package com.bella.backend.adapter.in.web.fluxocaixa;

import java.time.LocalDate;

/** O corpo é opcional: sem data efetiva, assume-se a data de hoje. */
public record RealizarMovimentacaoCaixaRequest(LocalDate dataEfetiva) {
}
