package com.bella.backend.adapter.in.web.auditoria;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditoriaIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JsonNode enviar(MockHttpServletRequestBuilder requisicao, String usuario, Object corpo,
                            int statusEsperado) throws Exception {
        if (usuario != null) {
            requisicao = requisicao.header("X-Usuario", usuario);
        }
        if (corpo != null) {
            requisicao = requisicao.contentType("application/json").content(objectMapper.writeValueAsString(corpo));
        }
        String resposta = mockMvc.perform(requisicao)
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
        return resposta.isBlank() ? null : objectMapper.readTree(resposta);
    }

    private String criarId(String url, String usuario, Map<String, Object> corpo) throws Exception {
        return enviar(post(url), usuario, corpo, 201).get("id").asText();
    }

    private Map<String, Object> corpoCliente(String nome, String cpf, String email, String celular) {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01001000");
        endereco.put("logradouro", "Rua A");
        endereco.put("numero", "10");
        endereco.put("bairro", "Centro");
        endereco.put("cidade", "Sao Paulo");
        endereco.put("uf", "SP");
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("tipoPessoa", "PF");
        cliente.put("nomeCompleto", nome);
        cliente.put("dataNascimento", "1990-05-10");
        cliente.put("cpf", cpf);
        cliente.put("celular", celular);
        cliente.put("email", email);
        cliente.put("endereco", endereco);
        return cliente;
    }

    private JsonNode historico(String modulo, String id) throws Exception {
        return enviar(get("/api/auditoria/historico/" + modulo + "/" + id), null, null, 200);
    }

    private JsonNode itens(String modulo, String id) throws Exception {
        return historico(modulo, id).get("itens");
    }

    @Test
    void criacaoRegistraUsuarioEstadoNovoESemValoresAnteriores() throws Exception {
        String id = criarId("/api/clientes", "ana.souza",
                corpoCliente("Cliente Auditado", "123.456.780-62", "auditado@example.com", "11999998888"));

        JsonNode registros = itens("CLIENTE", id);

        assertThat(registros).hasSize(1);
        JsonNode registro = registros.get(0);
        assertThat(registro.get("operacao").asText()).isEqualTo("CRIACAO");
        assertThat(registro.get("modulo").asText()).isEqualTo("CLIENTE");
        assertThat(registro.get("entidadeId").asText()).isEqualTo(id);
        assertThat(registro.get("usuario").asText()).isEqualTo("ana.souza");
        assertThat(registro.get("dataHora").asText()).isNotBlank();
        assertThat(registro.get("valoresAnteriores").isNull()).isTrue();
        assertThat(registro.get("novosValores").get("nomeCompleto").asText()).isEqualTo("Cliente Auditado");
    }

    @Test
    void semCabecalhoOResponsavelESistema() throws Exception {
        String id = criarId("/api/clientes", null,
                corpoCliente("Cliente Sistema", "234.567.891-73", "sistema@example.com", "11999998888"));

        assertThat(itens("CLIENTE", id).get(0).get("usuario").asText()).isEqualTo("sistema");
    }

    @Test
    void alteracaoRegistraSomenteOsCamposQueMudaramENaoAuditaEdicaoSemMudanca() throws Exception {
        String id = criarId("/api/clientes", "ana.souza",
                corpoCliente("Cliente Editado", "345.678.902-56", "editado@example.com", "11999998888"));

        enviar(put("/api/clientes/" + id), "bruno.lima",
                corpoCliente("Cliente Editado", "345.678.902-56", "editado@example.com", "11911112222"), 200);
        JsonNode aposEdicao = itens("CLIENTE", id);
        assertThat(aposEdicao).hasSize(2);
        JsonNode alteracao = aposEdicao.get(0);
        assertThat(alteracao.get("operacao").asText()).isEqualTo("ALTERACAO");
        assertThat(alteracao.get("usuario").asText()).isEqualTo("bruno.lima");
        assertThat(alteracao.get("valoresAnteriores").get("celular").asText()).isEqualTo("11999998888");
        assertThat(alteracao.get("novosValores").get("celular").asText()).isEqualTo("11911112222");
        assertThat(alteracao.get("novosValores").has("nomeCompleto")).isFalse();
        assertThat(alteracao.get("novosValores").has("atualizadoEm")).isFalse();

        enviar(put("/api/clientes/" + id), "bruno.lima",
                corpoCliente("Cliente Editado", "345.678.902-56", "editado@example.com", "11911112222"), 200);
        assertThat(itens("CLIENTE", id)).hasSize(2);
    }

    @Test
    void inativacaoEReativacaoSaoOperacoesProprias() throws Exception {
        String id = criarId("/api/clientes", "ana.souza",
                corpoCliente("Cliente Status", "456.789.013-20", "status@example.com", "11999998888"));

        enviar(patch("/api/clientes/" + id + "/status"), "ana.souza", Map.of("status", "INATIVO"), 200);
        enviar(patch("/api/clientes/" + id + "/status"), "ana.souza", Map.of("status", "ATIVO"), 200);

        JsonNode registros = itens("CLIENTE", id);
        assertThat(registros).hasSize(3);
        assertThat(registros.get(0).get("operacao").asText()).isEqualTo("REATIVACAO");
        assertThat(registros.get(1).get("operacao").asText()).isEqualTo("INATIVACAO");
        assertThat(registros.get(1).get("valoresAnteriores").get("status").asText()).isEqualTo("ATIVO");
        assertThat(registros.get(1).get("novosValores").get("status").asText()).isEqualTo("INATIVO");
        assertThat(registros.get(2).get("operacao").asText()).isEqualTo("CRIACAO");
    }

    @Test
    void cancelamentoGuardaOMotivoEPagamentoTemOperacaoPropria() throws Exception {
        String categoria = criarId("/api/categorias-despesa", null, Map.of("nome", "Auditoria Despesa"));
        Map<String, Object> conta = new LinkedHashMap<>();
        conta.put("descricao", "Conta auditada");
        conta.put("categoriaDespesaId", categoria);
        conta.put("valorTotal", "100.00");
        conta.put("vencimento", LocalDate.now().plusDays(10).toString());

        String paga = criarId("/api/contas-pagar", "financeiro", conta);
        String parcelaId = enviar(get("/api/contas-pagar/" + paga), null, null, 200)
                .get("parcelas").get(0).get("id").asText();
        Map<String, Object> pagamento = new LinkedHashMap<>();
        pagamento.put("parcelaId", parcelaId);
        pagamento.put("valorPago", "40.00");
        enviar(post("/api/contas-pagar/" + paga + "/pagamentos"), "financeiro", pagamento, 201);

        JsonNode pagamentos = itens("CONTA_PAGAR", paga);
        assertThat(pagamentos).hasSize(2);
        assertThat(pagamentos.get(0).get("operacao").asText()).isEqualTo("REGISTRO_PAGAMENTO");
        assertThat(pagamentos.get(0).get("valoresAnteriores").get("status").asText()).isEqualTo("PENDENTE");
        assertThat(pagamentos.get(0).get("novosValores").get("status").asText()).isEqualTo("PARCIALMENTE_PAGA");

        String cancelada = criarId("/api/contas-pagar", "financeiro", conta);
        enviar(patch("/api/contas-pagar/" + cancelada + "/cancelar"), "gerente",
                Map.of("motivo", "Lancada em duplicidade"), 200);

        JsonNode cancelamento = itens("CONTA_PAGAR", cancelada).get(0);
        assertThat(cancelamento.get("operacao").asText()).isEqualTo("CANCELAMENTO");
        assertThat(cancelamento.get("usuario").asText()).isEqualTo("gerente");
        assertThat(cancelamento.get("motivo").asText()).isEqualTo("Lancada em duplicidade");
        assertThat(cancelamento.get("novosValores").get("status").asText()).isEqualTo("CANCELADA");
    }

    @Test
    void movimentacaoManualDeEstoqueUsaOResponsavelInformadoNoComando() throws Exception {
        String categoria = criarId("/api/categorias", null, Map.of("nome", "Auditoria Estoque"));
        String unidade = criarId("/api/unidades-medida", null, Map.of("nome", "Unidade Aud", "sigla", "un"));
        Map<String, Object> produto = new LinkedHashMap<>();
        produto.put("codigoInterno", "SKU-AUD-1");
        produto.put("nome", "Produto Auditado");
        produto.put("categoriaId", categoria);
        produto.put("unidadeMedidaId", unidade);
        produto.put("precoCusto", "30.00");
        produto.put("precoVenda", "50.00");
        produto.put("estoqueMinimo", "0");
        String produtoId = criarId("/api/produtos", "ana.souza", produto);

        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("produtoId", produtoId);
        entrada.put("quantidade", "10");
        entrada.put("motivo", "Contagem inicial");
        entrada.put("usuarioResponsavel", "carlos.estoque");
        String movimentacaoId = criarId("/api/estoque/entradas", "ana.souza", entrada);

        JsonNode registro = itens("ESTOQUE", movimentacaoId).get(0);
        assertThat(registro.get("operacao").asText()).isEqualTo("MOVIMENTACAO_ESTOQUE");
        assertThat(registro.get("usuario").asText()).isEqualTo("carlos.estoque");
        assertThat(registro.get("motivo").asText()).isEqualTo("Contagem inicial");
        assertThat(itens("PRODUTO", produtoId).get(0).get("usuario").asText()).isEqualTo("ana.souza");
    }

    @Test
    void fluxoDeCompraAuditaStatusEAContaPagarGerada() throws Exception {
        String fornecedor = criarId("/api/fornecedores", null, Map.of("nome", "Fornecedor Auditoria"));
        String categoria = criarId("/api/categorias", null, Map.of("nome", "Auditoria Compra"));
        String unidade = criarId("/api/unidades-medida", null, Map.of("nome", "Unidade Cmp", "sigla", "un"));
        Map<String, Object> produto = new LinkedHashMap<>();
        produto.put("codigoInterno", "SKU-AUD-2");
        produto.put("nome", "Produto Compra Aud");
        produto.put("categoriaId", categoria);
        produto.put("unidadeMedidaId", unidade);
        produto.put("precoCusto", "30.00");
        produto.put("precoVenda", "50.00");
        produto.put("estoqueMinimo", "0");
        String produtoId = criarId("/api/produtos", null, produto);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "10");
        item.put("custoUnitario", "10.00");
        item.put("desconto", "0");
        Map<String, Object> compra = new LinkedHashMap<>();
        compra.put("fornecedorId", fornecedor);
        compra.put("previsaoRecebimento", LocalDate.now().plusDays(5).toString());
        compra.put("itens", List.of(item));
        compra.put("descontoGeral", "0");
        compra.put("frete", "0");
        compra.put("outrasDespesas", "0");
        compra.put("condicaoPagamento", "30 dias");
        String compraId = criarId("/api/compras", "comprador", compra);
        enviar(patch("/api/compras/" + compraId + "/confirmar"), "comprador", null, 200);
        enviar(patch("/api/compras/" + compraId + "/receber"), "recebedor", null, 200);

        JsonNode registros = itens("COMPRA", compraId);
        assertThat(registros).hasSize(3);
        assertThat(registros.get(0).get("operacao").asText()).isEqualTo("ALTERACAO_STATUS");
        assertThat(registros.get(0).get("usuario").asText()).isEqualTo("recebedor");
        assertThat(registros.get(0).get("novosValores").get("status").asText()).isEqualTo("RECEBIDA");

        JsonNode contasDaCompra = enviar(get("/api/auditoria").param("modulo", "CONTA_PAGAR")
                .param("usuario", "recebedor").param("operacao", "CRIACAO"), null, null, 200);
        assertThat(contasDaCompra.get("totalItens").asInt()).isEqualTo(1);
        assertThat(contasDaCompra.get("itens").get(0).get("novosValores").get("origem").asText())
                .isEqualTo("COMPRA");
    }

    @Test
    void consultaGeralFiltraPorUsuarioModuloOperacaoEPeriodo() throws Exception {
        criarId("/api/clientes", "filtro.maria",
                corpoCliente("Cliente Filtro 1", "567.890.124-94", "filtro1@example.com", "11999998888"));
        criarId("/api/clientes", "filtro.joao",
                corpoCliente("Cliente Filtro 2", "678.901.235-40", "filtro2@example.com", "11999998888"));
        criarId("/api/categorias", "filtro.maria", Map.of("nome", "Categoria sem auditoria"));

        JsonNode daMaria = enviar(get("/api/auditoria").param("usuario", "FILTRO.MARIA"), null, null, 200);
        assertThat(daMaria.get("totalItens").asInt()).isEqualTo(1);
        assertThat(daMaria.get("itens").get(0).get("modulo").asText()).isEqualTo("CLIENTE");

        JsonNode daMariaEmClientes = enviar(get("/api/auditoria").param("usuario", "filtro.maria")
                .param("modulo", "CLIENTE").param("operacao", "CRIACAO"), null, null, 200);
        assertThat(daMariaEmClientes.get("totalItens").asInt()).isEqualTo(1);
        JsonNode outroModulo = enviar(get("/api/auditoria").param("usuario", "filtro.maria")
                .param("modulo", "PRODUTO"), null, null, 200);
        assertThat(outroModulo.get("totalItens").asInt()).isZero();
        JsonNode soAlteracoes = enviar(get("/api/auditoria").param("usuario", "filtro.maria")
                .param("operacao", "ALTERACAO"), null, null, 200);
        assertThat(soAlteracoes.get("totalItens").asInt()).isZero();

        String hoje = LocalDate.now().toString();
        JsonNode hojeApenas = enviar(get("/api/auditoria").param("usuario", "filtro.joao")
                .param("inicio", hoje).param("fim", hoje), null, null, 200);
        assertThat(hojeApenas.get("totalItens").asInt()).isEqualTo(1);

        JsonNode passado = enviar(get("/api/auditoria").param("usuario", "filtro.joao")
                .param("inicio", "2000-01-01").param("fim", "2000-12-31"), null, null, 200);
        assertThat(passado.get("totalItens").asInt()).isZero();
        assertThat(passado.get("itens")).isEmpty();

        JsonNode pagina = enviar(get("/api/auditoria").param("modulo", "CLIENTE").param("tamanho", "1")
                .param("pagina", "1"), null, null, 200);
        assertThat(pagina.get("itens")).hasSize(1);
        assertThat(pagina.get("totalPaginas").asInt()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void buscaPorIdEValidacaoDosParametros() throws Exception {
        String id = criarId("/api/clientes", "ana.souza",
                corpoCliente("Cliente Busca", "789.012.346-96", "busca@example.com", "11999998888"));
        String registroId = itens("CLIENTE", id).get(0).get("id").asText();

        assertThat(enviar(get("/api/auditoria/" + registroId), null, null, 200).get("entidadeId").asText())
                .isEqualTo(id);
        mockMvc.perform(get("/api/auditoria/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/auditoria").param("inicio", "2026-02-10").param("fim", "2026-02-01"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/auditoria").param("tamanho", "0")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/auditoria").param("tamanho", "201")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/auditoria").param("pagina", "-1")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/auditoria").param("modulo", "INEXISTENTE")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/auditoria").param("operacao", "EXPLODIR")).andExpect(status().isBadRequest());
    }

    @Test
    void naoExisteEndpointParaCriarAlterarOuExcluirAuditoria() throws Exception {
        String qualquer = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/auditoria").contentType("application/json").content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/auditoria/" + qualquer).contentType("application/json").content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(patch("/api/auditoria/" + qualquer).contentType("application/json").content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/auditoria/" + qualquer)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void bancoRecusaAlterarExcluirOuTruncarRegistrosDeAuditoria() throws Exception {
        criarId("/api/clientes", "ana.souza",
                corpoCliente("Cliente Imutavel", "890.123.457-23", "imutavel@example.com", "11999998888"));

        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE bella.auditorias SET usuario = 'invasor'"))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("imutáveis");
        assertThatThrownBy(() -> jdbcTemplate.update("DELETE FROM bella.auditorias"))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("imutáveis");
        assertThatThrownBy(() -> jdbcTemplate.execute("TRUNCATE bella.auditorias"))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("imutáveis");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM bella.auditorias", Long.class))
                .isGreaterThan(0);
    }
}
