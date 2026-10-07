package com.bella.backend.adapter.in.web.compra;

import java.time.LocalDate;

/** O corpo é opcional: sem data de recebimento, assume-se a data de hoje. */
public record ReceberCompraRequest(LocalDate dataRecebimento) {
}
