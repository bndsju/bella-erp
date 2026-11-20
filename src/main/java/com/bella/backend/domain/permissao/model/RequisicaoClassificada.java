package com.bella.backend.domain.permissao.model;

import java.util.Optional;

/**
 * Traduz uma chamada da API ("PATCH /api/vendas/{id}/cancelar") em módulo e ação:
 * GET é VER; POST é CRIAR (inclui registrar pagamento, entrada de estoque, gerar pedido de um orçamento);
 * PUT e PATCH são EDITAR, exceto os que terminam em /cancelar ou /status, que são CANCELAR (cancelar, inativar, reativar).
 */
public record RequisicaoClassificada(ModuloSistema modulo, AcaoPermissao acao) {

    public static Optional<RequisicaoClassificada> de(String metodo, String caminho) {
        String limpo = caminho.length() > 1 && caminho.endsWith("/") ? caminho.substring(0, caminho.length() - 1) : caminho;
        return ModuloSistema.doCaminho(limpo).flatMap(modulo -> acaoDe(metodo, limpo)
                .filter(modulo::temAcao)
                .map(acao -> new RequisicaoClassificada(modulo, acao)));
    }

    private static Optional<AcaoPermissao> acaoDe(String metodo, String caminho) {
        return switch (metodo.toUpperCase()) {
            case "GET", "HEAD" -> Optional.of(AcaoPermissao.VER);
            case "POST" -> Optional.of(AcaoPermissao.CRIAR);
            case "PUT", "PATCH" -> Optional.of(caminho.endsWith("/cancelar") || caminho.endsWith("/status")
                    ? AcaoPermissao.CANCELAR : AcaoPermissao.EDITAR);
            default -> Optional.empty();
        };
    }
}
