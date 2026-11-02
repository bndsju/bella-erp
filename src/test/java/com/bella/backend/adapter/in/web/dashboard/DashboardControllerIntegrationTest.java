package com.bella.backend.adapter.in.web.dashboard;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode enviar(MockHttpServletRequestBuilder requisicao, Object corpo, int statusEsperado)
            throws Exception {
        if (corpo != null) {
            requisicao = requisicao.contentType("application/json").content(objectMapper.writeValueAsString(corpo));
        }
        String resposta = mockMvc.perform(requisicao)
                .andExpect(status().is(statusEsperado))
                .andReturn().getResponse().getContentAsString();
        return resposta.isBlank() ? null : objectMapper.readTree(resposta);
    }

    private String criarId(String url, Map<String, Object> corpo) throws Exception {
        return enviar(post(url), corpo, 201).get("id").asText();
    }

    private String criarProduto(String sufixo, String estoqueMinimo) throws Exception {
        String categoriaId = criarId("/api/categorias", Map.of("nome", "Categoria " + sufixo));
        Map<String, Object> unidade = new LinkedHashMap<>();
        unidade.put("nome", "Unidade " + sufixo);
        unidade.put("sigla", "un");
        String unidadeId = criarId("/api/unidades-medida", unidade);

        Map<String, Object> produto = new LinkedHashMap<>();
        produto.put("codigoInterno", "SKU-DSH-" + sufixo);
        produto.put("nome", "Produto " + sufixo);
        produto.put("categoriaId", categoriaId);
        produto.put("unidadeMedidaId", unidadeId);
        produto.put("precoCusto", "30.00");
        produto.put("precoVenda", "50.00");
        produto.put("estoqueMinimo", estoqueMinimo);
        return criarId("/api/produtos", produto);
    }

    private void darEntrada(String produtoId, String quantidade) throws Exception {
        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("produtoId", produtoId);
        entrada.put("quantidade", quantidade);
        entrada.put("motivo", "Estoque inicial para o teste");
        criarId("/api/estoque/entradas", entrada);
    }

    private String criarCliente() throws Exception {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01001000");
        endereco.put("logradouro", "Rua A");
        endereco.put("numero", "10");
        endereco.put("bairro", "Centro");
        endereco.put("cidade", "Sao Paulo");
        endereco.put("uf", "SP");
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("tipoPessoa", "PF");
        cliente.put("nomeCompleto", "Maria Dashboard");
        cliente.put("dataNascimento", "1990-05-10");
        cliente.put("cpf", "111.444.777-35");
        cliente.put("celular", "11999998888");
        cliente.put("email", "maria-dashboard@example.com");
        cliente.put("endereco", endereco);
        return criarId("/api/clientes", cliente);
    }

    private String criarOrcamento(String clienteId, String produtoId, String valorUnitario) throws Exception {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "2");
        item.put("valorUnitario", valorUnitario);
        Map<String, Object> orcamento = new LinkedHashMap<>();
        orcamento.put("clienteId", clienteId);
        orcamento.put("itens", List.of(item));
        orcamento.put("percentualDesconto", "0");
        orcamento.put("valorFrete", "0");
        orcamento.put("prazoValidade", LocalDate.now().plusDays(7).toString());
        orcamento.put("condicaoPagamento", "À vista");
        return criarId("/api/orcamentos", orcamento);
    }

    private JsonNode dashboard(String inicio, String fim) throws Exception {
        MockHttpServletRequestBuilder requisicao = get("/api/dashboard");
        if (inicio != null) {
            requisicao = requisicao.param("inicio", inicio);
        }
        if (fim != null) {
            requisicao = requisicao.param("fim", fim);
        }
        return enviar(requisicao, null, 200);
    }

    @Test
    void consolidaIndicadoresDosModulosNoPeriodo() throws Exception {
        String clienteId = criarCliente();
        String produtoVendido = criarProduto("A", "5");
        String produtoEscasso = criarProduto("B", "10");
        darEntrada(produtoVendido, "100");
        darEntrada(produtoEscasso, "5");

        // orçamento 1: aprovado, vira pedido, é entregue e concluído em venda (2 x 50 = 100)
        String orcamentoConvertido = criarOrcamento(clienteId, produtoVendido, "50.00");
        enviar(patch("/api/orcamentos/" + orcamentoConvertido + "/enviar"), null, 200);
        enviar(patch("/api/orcamentos/" + orcamentoConvertido + "/aprovar"), null, 200);
        String pedidoId = enviar(post("/api/pedidos/a-partir-de-orcamento"),
                Map.of("orcamentoId", orcamentoConvertido), 201).get("id").asText();
        for (String passo : List.of("confirmar", "iniciar-separacao", "marcar-pronto-para-entrega", "entregar")) {
            enviar(patch("/api/pedidos/" + pedidoId + "/" + passo), null, 200);
        }
        enviar(post("/api/vendas"), Map.of("pedidoId", pedidoId, "formaPagamento", "PIX"), 201);

        // orçamento 2: enviado e sem andamento (2 x 20 = 40); orçamento 3: rascunho, não conta como emitido
        String orcamentoParado = criarOrcamento(clienteId, produtoVendido, "20.00");
        enviar(patch("/api/orcamentos/" + orcamentoParado + "/enviar"), null, 200);
        criarOrcamento(clienteId, produtoVendido, "10.00");

        // financeiro: conta a pagar vencida e entrada realizada no caixa
        String categoriaDespesa = criarId("/api/categorias-despesa", Map.of("nome", "Aluguel"));
        Map<String, Object> contaPagar = new LinkedHashMap<>();
        contaPagar.put("descricao", "Aluguel atrasado");
        contaPagar.put("categoriaDespesaId", categoriaDespesa);
        contaPagar.put("valorTotal", "300.00");
        contaPagar.put("vencimento", LocalDate.now().minusDays(1).toString());
        criarId("/api/contas-pagar", contaPagar);

        String categoriaCaixa = criarId("/api/categorias-fluxo-caixa", Map.of("nome", "Aportes"));
        Map<String, Object> aporte = new LinkedHashMap<>();
        aporte.put("tipo", "ENTRADA");
        aporte.put("descricao", "Aporte");
        aporte.put("categoriaId", categoriaCaixa);
        aporte.put("valor", "500.00");
        aporte.put("situacao", "REALIZADO");
        aporte.put("dataPrevista", LocalDate.now().toString());
        aporte.put("dataEfetiva", LocalDate.now().toString());
        criarId("/api/fluxo-caixa/movimentacoes", aporte);

        JsonNode resultado = dashboard(null, null);

        JsonNode comercial = resultado.get("comercial");
        assertThat(comercial.get("faturamento").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(comercial.get("quantidadeVendas").asInt()).isEqualTo(1);
        assertThat(comercial.get("ticketMedio").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(comercial.get("quantidadePedidos").asInt()).isEqualTo(1);
        assertThat(comercial.get("orcamentosEmitidos").asInt()).isEqualTo(2);
        assertThat(comercial.get("valorOrcamentosEmitidos").decimalValue()).isEqualByComparingTo("140.00");
        assertThat(comercial.get("taxaConversaoPedidos").decimalValue()).isEqualByComparingTo("50.0");
        assertThat(comercial.get("taxaConversaoVendas").decimalValue()).isEqualByComparingTo("50.0");

        double faturamentoDaSerie = 0;
        for (JsonNode ponto : comercial.get("vendasPorPeriodo")) {
            faturamentoDaSerie += ponto.get("valor").asDouble();
        }
        assertThat(faturamentoDaSerie).isEqualTo(100.0);

        JsonNode financeiro = resultado.get("financeiro");
        JsonNode pagar = financeiro.get("contasPagar");
        assertThat(pagar.get("emAberto").decimalValue()).isEqualByComparingTo("300.00");
        assertThat(pagar.get("vencido").decimalValue()).isEqualByComparingTo("300.00");
        assertThat(pagar.get("aVencer").decimalValue()).isEqualByComparingTo("0");
        assertThat(pagar.get("quantidadeContasVencidas").asInt()).isEqualTo(1);
        JsonNode receber = financeiro.get("contasReceber");
        assertThat(receber.get("emAberto").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(receber.get("vencido").decimalValue()).isEqualByComparingTo("0");
        assertThat(financeiro.get("saldoAtual").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(financeiro.get("entradasPeriodo").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(financeiro.get("saidasPeriodo").decimalValue()).isEqualByComparingTo("0");

        JsonNode maisVendidos = resultado.get("produtosMaisVendidos");
        assertThat(maisVendidos).hasSize(1);
        assertThat(maisVendidos.get(0).get("nome").asText()).isEqualTo("Produto A");
        assertThat(maisVendidos.get(0).get("quantidade").decimalValue()).isEqualByComparingTo("2");
        assertThat(maisVendidos.get(0).get("valor").decimalValue()).isEqualByComparingTo("100.00");

        JsonNode clientes = resultado.get("principaisClientes");
        assertThat(clientes).hasSize(1);
        assertThat(clientes.get(0).get("nome").asText()).isEqualTo("Maria Dashboard");
        assertThat(clientes.get(0).get("valor").decimalValue()).isEqualByComparingTo("100.00");

        assertThat(resultado.get("totalProdutosAbaixoDoMinimo").asInt()).isEqualTo(1);
        assertThat(resultado.get("produtosAbaixoDoMinimo").get(0).get("nome").asText()).isEqualTo("Produto B");
    }

    @Test
    void periodoSemMovimentacaoRetornaZerosESerieCompleta() throws Exception {
        JsonNode resultado = dashboard("2000-01-01", "2000-01-03");

        JsonNode comercial = resultado.get("comercial");
        assertThat(comercial.get("faturamento").decimalValue()).isEqualByComparingTo("0");
        assertThat(comercial.get("quantidadeVendas").asInt()).isZero();
        assertThat(comercial.get("ticketMedio").decimalValue()).isEqualByComparingTo("0");
        assertThat(comercial.get("taxaConversaoPedidos").isNull()).isTrue();
        assertThat(comercial.get("vendasPorPeriodo")).hasSize(3);
        assertThat(resultado.get("financeiro").get("fluxoPorPeriodo")).hasSize(3);
        assertThat(resultado.get("produtosMaisVendidos")).isEmpty();
        assertThat(resultado.get("principaisClientes")).isEmpty();
        assertThat(resultado.get("periodo").get("granularidade").asText()).isEqualTo("DIA");
    }

    @Test
    void periodosLongosSaoAgrupadosPorMes() throws Exception {
        JsonNode resultado = dashboard("2025-01-15", "2025-12-31");

        assertThat(resultado.get("periodo").get("granularidade").asText()).isEqualTo("MES");
        assertThat(resultado.get("comercial").get("vendasPorPeriodo")).hasSize(12);
        assertThat(resultado.get("comercial").get("vendasPorPeriodo").get(0).get("data").asText())
                .isEqualTo("2025-01-01");
    }

    @Test
    void rejeitaPeriodoComFimAnteriorAoInicio() throws Exception {
        mockMvc.perform(get("/api/dashboard").param("inicio", "2026-02-10").param("fim", "2026-02-01"))
                .andExpect(status().isUnprocessableEntity());
    }
}
