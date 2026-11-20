package com.bella.backend.domain.permissao.model;

/**
 * O que se pode fazer em um módulo. O sistema não apaga registros: o que seria "excluir" é cancelar ou inativar,
 * por isso a quarta ação se chama CANCELAR.
 */
public enum AcaoPermissao {
    VER("Ver"),
    CRIAR("Criar"),
    EDITAR("Editar"),
    CANCELAR("Cancelar / inativar");

    private final String rotulo;

    AcaoPermissao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
