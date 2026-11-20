package com.bella.backend.domain.permissao.model;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static com.bella.backend.domain.permissao.model.AcaoPermissao.CANCELAR;
import static com.bella.backend.domain.permissao.model.AcaoPermissao.CRIAR;
import static com.bella.backend.domain.permissao.model.AcaoPermissao.EDITAR;
import static com.bella.backend.domain.permissao.model.AcaoPermissao.VER;

/**
 * Módulos sobre os quais o administrador distribui permissões. Cada um sabe quais ações existem nele (só aparecem
 * na tela as que de fato acontecem) e quais caminhos da API pertencem a ele. Usuários e permissões ficam de fora:
 * são sempre exclusivos do administrador.
 */
public enum ModuloSistema {
    DASHBOARD("Início (painel)", "Geral", EnumSet.of(VER), "/api/dashboard"),
    CLIENTES("Clientes", "Cadastros", EnumSet.allOf(AcaoPermissao.class), "/api/clientes"),
    PRODUTOS("Produtos, categorias e unidades", "Cadastros", EnumSet.allOf(AcaoPermissao.class),
            "/api/produtos", "/api/categorias", "/api/unidades-medida"),
    FORNECEDORES("Fornecedores", "Cadastros", EnumSet.allOf(AcaoPermissao.class), "/api/fornecedores"),
    TRANSPORTADORAS("Transportadoras", "Cadastros", EnumSet.allOf(AcaoPermissao.class), "/api/transportadoras"),
    ORCAMENTOS("Orçamentos", "Comercial", EnumSet.allOf(AcaoPermissao.class), "/api/orcamentos"),
    PEDIDOS("Pedidos", "Comercial", EnumSet.allOf(AcaoPermissao.class), "/api/pedidos"),
    VENDAS("Vendas", "Comercial", EnumSet.of(VER, CRIAR, CANCELAR), "/api/vendas"),
    COMPRAS("Compras", "Suprimentos", EnumSet.allOf(AcaoPermissao.class), "/api/compras"),
    ESTOQUE("Estoque", "Suprimentos", EnumSet.of(VER, CRIAR), "/api/estoque"),
    CONTAS_RECEBER("Contas a receber", "Financeiro", EnumSet.allOf(AcaoPermissao.class), "/api/contas-receber"),
    CONTAS_PAGAR("Contas a pagar e categorias de despesa", "Financeiro", EnumSet.allOf(AcaoPermissao.class),
            "/api/contas-pagar", "/api/categorias-despesa"),
    FLUXO_CAIXA("Fluxo de caixa e categorias de caixa", "Financeiro", EnumSet.allOf(AcaoPermissao.class),
            "/api/fluxo-caixa", "/api/categorias-fluxo-caixa"),
    RELATORIOS("Relatórios", "Gestão", EnumSet.of(VER), "/api/relatorios"),
    AUDITORIA("Auditoria", "Gestão", EnumSet.of(VER), "/api/auditoria");

    private final String rotulo;
    private final String grupo;
    private final Set<AcaoPermissao> acoes;
    private final String[] prefixos;

    ModuloSistema(String rotulo, String grupo, Set<AcaoPermissao> acoes, String... prefixos) {
        this.rotulo = rotulo;
        this.grupo = grupo;
        this.acoes = acoes;
        this.prefixos = prefixos;
    }

    public String rotulo() {
        return rotulo;
    }

    public String grupo() {
        return grupo;
    }

    public Set<AcaoPermissao> acoesDisponiveis() {
        return acoes;
    }

    public boolean temAcao(AcaoPermissao acao) {
        return acoes.contains(acao);
    }

    private boolean contem(String caminho) {
        for (String prefixo : prefixos) {
            if (caminho.equals(prefixo) || caminho.startsWith(prefixo + "/")) {
                return true;
            }
        }
        return false;
    }

    /** Módulo dono de um caminho da API; vazio se o caminho não pertence a nenhum (nesse caso só o administrador entra). */
    public static Optional<ModuloSistema> doCaminho(String caminho) {
        for (ModuloSistema modulo : values()) {
            if (modulo.contem(caminho)) {
                return Optional.of(modulo);
            }
        }
        return Optional.empty();
    }
}
