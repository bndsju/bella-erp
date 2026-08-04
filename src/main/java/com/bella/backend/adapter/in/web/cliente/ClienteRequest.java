package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.domain.cliente.port.in.ComandoCliente;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Corpo da requisição de cadastro/edição de cliente. O campo "tipoPessoa" ("PF" ou "PJ")
 * decide qual subtipo o Jackson usa para desserializar, espelhando o formulário do
 * front-end que alterna entre Pessoa Física e Pessoa Jurídica.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tipoPessoa", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClientePessoaFisicaRequest.class, name = "PF"),
        @JsonSubTypes.Type(value = ClientePessoaJuridicaRequest.class, name = "PJ")
})
public sealed interface ClienteRequest permits ClientePessoaFisicaRequest, ClientePessoaJuridicaRequest {

    ComandoCliente paraComando();
}
