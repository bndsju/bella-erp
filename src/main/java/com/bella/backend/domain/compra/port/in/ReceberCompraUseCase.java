package com.bella.backend.domain.compra.port.in;

import com.bella.backend.domain.compra.model.Compra;

import java.time.LocalDate;
import java.util.UUID;

public interface ReceberCompraUseCase {

    /** Se a data de recebimento for nula, assume a data de hoje. */
    Compra receber(UUID id, LocalDate dataRecebimento);
}
