package com.bella.backend.domain.categoriadespesa.model;

import java.util.UUID;

/** Categorias criadas por migração e usadas por contas a pagar geradas automaticamente. */
public final class CategoriasDespesaDoSistema {

    public static final UUID COMPRA_DE_MERCADORIAS = UUID.fromString("00000000-0000-0000-0000-0000000c0001");

    private CategoriasDespesaDoSistema() {
    }
}
