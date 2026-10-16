package com.bella.backend.domain.contareceber.model;

/**
 * Como a conta foi lançada. VENDA fica reservada para a integração futura em que uma venda
 * concluída gera a conta a receber (a referência de origem guarda o id da venda).
 */
public enum OrigemContaReceber {
    MANUAL,
    VENDA
}
