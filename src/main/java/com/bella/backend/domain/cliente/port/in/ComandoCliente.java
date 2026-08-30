package com.bella.backend.domain.cliente.port.in;

public sealed interface ComandoCliente permits ComandoClientePessoaFisica, ComandoClientePessoaJuridica {
}
