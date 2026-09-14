package com.bella.backend.domain.venda.port.in;

import com.bella.backend.domain.venda.model.FormaPagamento;
import com.bella.backend.domain.venda.model.Venda;

import java.util.UUID;

public interface ConcluirVendaUseCase {

    Venda concluir(UUID pedidoId, FormaPagamento formaPagamento);
}
