package com.bella.backend.domain.contapagar.model;

/**
 * Como a conta foi lançada. COMPRA fica reservada para a integração futura em que uma compra
 * recebida gera a conta a pagar (a referência de origem guarda o id da compra).
 */
public enum OrigemContaPagar {
    MANUAL,
    COMPRA
}
