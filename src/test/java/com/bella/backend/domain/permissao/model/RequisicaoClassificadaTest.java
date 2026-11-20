package com.bella.backend.domain.permissao.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RequisicaoClassificadaTest {

    @ParameterizedTest
    @CsvSource({
            "GET,/api/clientes,CLIENTES,VER",
            "GET,/api/clientes/123,CLIENTES,VER",
            "POST,/api/clientes,CLIENTES,CRIAR",
            "PUT,/api/clientes/123,CLIENTES,EDITAR",
            "PATCH,/api/clientes/123/status,CLIENTES,CANCELAR",
            "PATCH,/api/vendas/123/cancelar,VENDAS,CANCELAR",
            "POST,/api/pedidos/a-partir-de-orcamento,PEDIDOS,CRIAR",
            "PATCH,/api/pedidos/123/confirmar,PEDIDOS,EDITAR",
            "POST,/api/contas-pagar/123/pagamentos,CONTAS_PAGAR,CRIAR",
            "PATCH,/api/contas-receber/123/dados-basicos,CONTAS_RECEBER,EDITAR",
            "GET,/api/categorias-despesa,CONTAS_PAGAR,VER",
            "GET,/api/categorias-fluxo-caixa,FLUXO_CAIXA,VER",
            "GET,/api/categorias,PRODUTOS,VER",
            "GET,/api/unidades-medida,PRODUTOS,VER",
            "POST,/api/estoque/entradas,ESTOQUE,CRIAR",
            "GET,/api/estoque/movimentacoes,ESTOQUE,VER",
            "GET,/api/relatorios/estoque/posicao,RELATORIOS,VER",
            "GET,/api/auditoria/historico/CLIENTE/123,AUDITORIA,VER",
            "GET,/api/dashboard,DASHBOARD,VER",
            "GET,/api/clientes/,CLIENTES,VER",
    })
    void classificaMetodoECaminho(String metodo, String caminho, ModuloSistema modulo, AcaoPermissao acao) {
        assertThat(RequisicaoClassificada.de(metodo, caminho))
                .contains(new RequisicaoClassificada(modulo, acao));
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/api/usuarios",
            "GET,/api/permissoes",
            "GET,/api/qualquer-coisa",
            "GET,/api/clientes-falsos",
            "DELETE,/api/clientes/123",
            "POST,/api/relatorios/vendas",
            "PUT,/api/vendas/123",
            "PATCH,/api/estoque/algo",
    })
    void caminhosOuAcoesSemModuloNaoSaoClassificados(String metodo, String caminho) {
        assertThat(RequisicaoClassificada.de(metodo, caminho)).isEmpty();
    }

    @Test
    void padraoDoVendedorNaoTemFinanceiroNemAuditoriaMasTemOperacao() {
        var padrao = PermissoesPadrao.de(com.bella.backend.domain.usuario.model.PerfilUsuario.VENDEDOR);

        assertThat(padrao).doesNotContainKeys(ModuloSistema.CONTAS_PAGAR, ModuloSistema.CONTAS_RECEBER,
                ModuloSistema.FLUXO_CAIXA, ModuloSistema.RELATORIOS, ModuloSistema.AUDITORIA);
        assertThat(padrao.get(ModuloSistema.CLIENTES)).containsExactlyInAnyOrder(AcaoPermissao.values());
        assertThat(padrao.get(ModuloSistema.DASHBOARD)).containsExactly(AcaoPermissao.VER);
    }

    @Test
    void todoModuloOferecePeloMenosVerEPadraoRespeitaAsAcoesDoModulo() {
        for (ModuloSistema modulo : ModuloSistema.values()) {
            assertThat(modulo.temAcao(AcaoPermissao.VER)).as(modulo.name()).isTrue();
        }
        for (var perfil : com.bella.backend.domain.usuario.model.PerfilUsuario.values()) {
            PermissoesPadrao.de(perfil).forEach((modulo, acoes) ->
                    assertThat(modulo.acoesDisponiveis()).containsAll(acoes));
        }
    }
}
