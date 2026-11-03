package com.bella.backend.adapter.in.web.integracao;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IntegracaoEntreModulosIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private JsonNode enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
                            Object corpo, int statusEsperado) throws Exception {
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

    private String criarProduto(String sufixo) throws Exception {
        String categoriaId = criarId("/api/categorias", Map.of("nome", "Categoria " + sufixo));
        Map<String, Object> unidade = new LinkedHashMap<>();
        unidade.put("nome", "Unidade " + sufixo);
        unidade.put("sigla", "un");
        String unidadeId = criarId("/api/unidades-medida", unidade);

        Map<String, Object> produto = new LinkedHashMap<>();
        produto.put("codigoInterno", "SKU-INT-" + sufixo);
        produto.put("nome", "Produto " + sufixo);
        produto.put("categoriaId", categoriaId);
        produto.put("unidadeMedidaId", unidadeId);
        produto.put("precoCusto", "30.00");
        produto.put("precoVenda", "50.00");
        produto.put("estoqueMinimo", "0");
        return criarId("/api/produtos", produto);
    }

    private String criarCompraRecebida(String condicaoPagamento, String sufixo) throws Exception {
        String fornecedorId = criarId("/api/fornecedores", Map.of("nome", "Fornecedor " + sufixo));
        String produtoId = criarProduto(sufixo);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "10");
        item.put("custoUnitario", "10.00");
        item.put("desconto", "0");

        Map<String, Object> compra = new LinkedHashMap<>();
        compra.put("fornecedorId", fornecedorId);
        compra.put("previsaoRecebimento", LocalDate.now().plusDays(5).toString());
        compra.put("itens", List.of(item));
        compra.put("descontoGeral", "0");
        compra.put("frete", "0");
        compra.put("outrasDespesas", "0");
        compra.put("condicaoPagamento", condicaoPagamento);

        String compraId = criarId("/api/compras", compra);
        enviar(patch("/api/compras/" + compraId + "/confirmar"), null, 200);
        enviar(patch("/api/compras/" + compraId + "/receber"), null, 200);
        return compraId;
    }

    private JsonNode contaPagarDaCompra(String compraId) throws Exception {
        for (JsonNode conta : enviar(get("/api/contas-pagar"), null, 200)) {
            if (compraId.equals(conta.path("origemReferenciaId").asText())) {
                return conta;
            }
        }
        throw new AssertionError("Conta a pagar da compra não encontrada: " + compraId);
    }

    private JsonNode movimentacoes(String origem) throws Exception {
        return enviar(get("/api/fluxo-caixa/movimentacoes").param("origem", origem)
                .param("incluirCanceladas", "true"), null, 200);
    }

    private JsonNode movimentacaoDaReferencia(String origem, String referenciaId) throws Exception {
        for (JsonNode movimentacao : movimentacoes(origem)) {
            if (referenciaId.equals(movimentacao.path("origemReferenciaId").asText())) {
                return movimentacao;
            }
        }
        throw new AssertionError("Movimentação não encontrada para a referência " + referenciaId);
    }

    @Test
    void recebimentoDaCompraGeraContaPagarParceladaEProjecaoNoCaixa() throws Exception {
        String compraId = criarCompraRecebida("30/60 dias", "A");

        JsonNode conta = contaPagarDaCompra(compraId);
        org.assertj.core.api.Assertions.assertThat(conta.get("origem").asText()).isEqualTo("COMPRA");
        org.assertj.core.api.Assertions.assertThat(conta.get("categoriaDespesaNome").asText())
                .isEqualTo("Compra de mercadorias");
        org.assertj.core.api.Assertions.assertThat(conta.get("valorTotal").decimalValue())
                .isEqualByComparingTo("100.00");
        org.assertj.core.api.Assertions.assertThat(conta.get("parcelas")).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(conta.get("parcelas").get(0).get("vencimento").asText())
                .isEqualTo(LocalDate.now().plusDays(30).toString());

        for (JsonNode parcela : conta.get("parcelas")) {
            JsonNode movimentacao = movimentacaoDaReferencia("CONTA_PAGAR", parcela.get("id").asText());
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("tipo").asText()).isEqualTo("SAIDA");
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("situacao").asText())
                    .isEqualTo("PREVISTO");
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("valor").decimalValue())
                    .isEqualByComparingTo("50.00");
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("dataPrevista").asText())
                    .isEqualTo(parcela.get("vencimento").asText());
        }
    }

    @Test
    void pagamentoParcialRealizaOValorDesembolsadoEProjetaOSaldo() throws Exception {
        String compraId = criarCompraRecebida("30 dias", "B");
        JsonNode conta = contaPagarDaCompra(compraId);
        String contaId = conta.get("id").asText();
        String parcelaId = conta.get("parcelas").get(0).get("id").asText();

        Map<String, Object> pagamento = new LinkedHashMap<>();
        pagamento.put("parcelaId", parcelaId);
        pagamento.put("valorPago", "40.00");
        pagamento.put("juros", "2.00");
        enviar(post("/api/contas-pagar/" + contaId + "/pagamentos"), pagamento, 201);

        JsonNode atualizada = enviar(get("/api/contas-pagar/" + contaId), null, 200);
        String pagamentoId = atualizada.get("parcelas").get(0).get("pagamentos").get(0).get("id").asText();

        JsonNode realizada = movimentacaoDaReferencia("CONTA_PAGAR", pagamentoId);
        org.assertj.core.api.Assertions.assertThat(realizada.get("situacao").asText()).isEqualTo("REALIZADO");
        org.assertj.core.api.Assertions.assertThat(realizada.get("valor").decimalValue())
                .isEqualByComparingTo("42.00");
        org.assertj.core.api.Assertions.assertThat(realizada.get("dataEfetiva").asText())
                .isEqualTo(LocalDate.now().toString());

        JsonNode projecaoOriginal = movimentacaoDaReferencia("CONTA_PAGAR", parcelaId);
        org.assertj.core.api.Assertions.assertThat(projecaoOriginal.get("cancelada").asBoolean()).isTrue();

        long projecoesVigentes = 0;
        for (JsonNode movimentacao : movimentacoes("CONTA_PAGAR")) {
            if ("PREVISTO".equals(movimentacao.get("situacao").asText())
                    && !movimentacao.get("cancelada").asBoolean()
                    && movimentacao.get("descricao").asText().contains("Compra " + compraId.substring(0, 8))) {
                projecoesVigentes++;
                org.assertj.core.api.Assertions.assertThat(movimentacao.get("valor").decimalValue())
                        .isEqualByComparingTo("60.00");
            }
        }
        org.assertj.core.api.Assertions.assertThat(projecoesVigentes).isEqualTo(1);

        Map<String, Object> restante = new LinkedHashMap<>();
        restante.put("parcelaId", parcelaId);
        restante.put("valorPago", "60.00");
        enviar(post("/api/contas-pagar/" + contaId + "/pagamentos"), restante, 201);

        for (JsonNode movimentacao : movimentacoes("CONTA_PAGAR")) {
            if (movimentacao.get("descricao").asText().contains("Compra " + compraId.substring(0, 8))) {
                org.assertj.core.api.Assertions.assertThat(
                        movimentacao.get("cancelada").asBoolean()
                                || "REALIZADO".equals(movimentacao.get("situacao").asText())).isTrue();
            }
        }
    }

    @Test
    void cancelarContaPagarCancelaAsProjecoes() throws Exception {
        String compraId = criarCompraRecebida("30/60/90", "C");
        JsonNode conta = contaPagarDaCompra(compraId);

        Map<String, Object> cancelamento = Map.of("motivo", "Lançada em duplicidade");
        enviar(patch("/api/contas-pagar/" + conta.get("id").asText() + "/cancelar"), cancelamento, 200);

        org.assertj.core.api.Assertions.assertThat(conta.get("parcelas")).hasSize(3);
        for (JsonNode parcela : conta.get("parcelas")) {
            JsonNode movimentacao = movimentacaoDaReferencia("CONTA_PAGAR", parcela.get("id").asText());
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("cancelada").asBoolean()).isTrue();
        }
    }

    @Test
    void editarValorEParcelasSubstituiAsProjecoes() throws Exception {
        String categoria = criarId("/api/categorias-despesa", Map.of("nome", "Categoria edicao D"));
        Map<String, Object> manual = new LinkedHashMap<>();
        manual.put("descricao", "Conta manual editavel");
        manual.put("categoriaDespesaId", categoria);
        manual.put("valorTotal", "100.00");
        manual.put("vencimento", LocalDate.now().plusDays(30).toString());
        JsonNode conta = enviar(post("/api/contas-pagar"), manual, 201);
        String parcelaAntiga = conta.get("parcelas").get(0).get("id").asText();

        Map<String, Object> edicao = new LinkedHashMap<>();
        edicao.put("descricao", conta.get("descricao").asText());
        edicao.put("categoriaDespesaId", categoria);
        edicao.put("valorTotal", "100.00");
        edicao.put("parcelas", List.of(
                Map.of("valor", "70.00", "vencimento", LocalDate.now().plusDays(10).toString()),
                Map.of("valor", "30.00", "vencimento", LocalDate.now().plusDays(20).toString())));
        JsonNode editada = enviar(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/contas-pagar/" + conta.get("id").asText()), edicao, 200);

        org.assertj.core.api.Assertions.assertThat(
                movimentacaoDaReferencia("CONTA_PAGAR", parcelaAntiga).get("cancelada").asBoolean()).isTrue();
        for (JsonNode parcela : editada.get("parcelas")) {
            JsonNode movimentacao = movimentacaoDaReferencia("CONTA_PAGAR", parcela.get("id").asText());
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("cancelada").asBoolean()).isFalse();
            org.assertj.core.api.Assertions.assertThat(movimentacao.get("valor").decimalValue())
                    .isEqualByComparingTo(parcela.get("valor").decimalValue());
        }
    }

    private String criarVendaConcluida(String condicaoPagamento, String sufixo) throws Exception {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01001000");
        endereco.put("logradouro", "Rua A");
        endereco.put("numero", "10");
        endereco.put("bairro", "Centro");
        endereco.put("cidade", "Sao Paulo");
        endereco.put("uf", "SP");
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("tipoPessoa", "PF");
        cliente.put("nomeCompleto", "Cliente " + sufixo);
        cliente.put("dataNascimento", "1990-05-10");
        cliente.put("cpf", Map.of("E", "111.444.777-35", "F", "390.533.447-05", "G", "529.982.247-25",
                "H", "123.456.780-62", "I", "234.567.891-73").get(sufixo));
        cliente.put("celular", "11999998888");
        cliente.put("email", "cliente-" + sufixo.toLowerCase() + "@example.com");
        cliente.put("endereco", endereco);
        String clienteId = criarId("/api/clientes", cliente);

        String produtoId = criarProduto(sufixo);
        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("produtoId", produtoId);
        entrada.put("quantidade", "100");
        entrada.put("motivo", "Estoque inicial para o teste");
        criarId("/api/estoque/entradas", entrada);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "2");
        item.put("valorUnitario", "50.00");
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("clienteId", clienteId);
        pedido.put("itens", List.of(item));
        pedido.put("percentualDesconto", "0");
        pedido.put("valorFrete", "0");
        pedido.put("condicaoPagamento", condicaoPagamento);
        String pedidoId = criarId("/api/pedidos", pedido);
        for (String passo : List.of("confirmar", "iniciar-separacao", "marcar-pronto-para-entrega", "entregar")) {
            enviar(patch("/api/pedidos/" + pedidoId + "/" + passo), null, 200);
        }

        Map<String, Object> venda = new LinkedHashMap<>();
        venda.put("pedidoId", pedidoId);
        venda.put("formaPagamento", "BOLETO");
        return criarId("/api/vendas", venda);
    }

    private JsonNode contaReceberDaVenda(String vendaId) throws Exception {
        for (JsonNode conta : enviar(get("/api/contas-receber"), null, 200)) {
            if (vendaId.equals(conta.path("origemReferenciaId").asText())) {
                return conta;
            }
        }
        throw new AssertionError("Conta a receber da venda não encontrada: " + vendaId);
    }

    @Test
    void vendaConcluidaGeraContaReceberEEntradaPrevista() throws Exception {
        String vendaId = criarVendaConcluida("2x", "E");

        JsonNode conta = contaReceberDaVenda(vendaId);
        org.assertj.core.api.Assertions.assertThat(conta.get("origem").asText()).isEqualTo("VENDA");
        org.assertj.core.api.Assertions.assertThat(conta.get("valorTotal").decimalValue())
                .isEqualByComparingTo("100.00");
        org.assertj.core.api.Assertions.assertThat(conta.get("parcelas")).hasSize(2);

        String parcelaId = conta.get("parcelas").get(0).get("id").asText();
        JsonNode previsto = movimentacaoDaReferencia("CONTA_RECEBER", parcelaId);
        org.assertj.core.api.Assertions.assertThat(previsto.get("tipo").asText()).isEqualTo("ENTRADA");
        org.assertj.core.api.Assertions.assertThat(previsto.get("situacao").asText()).isEqualTo("PREVISTO");

        Map<String, Object> recebimento = new LinkedHashMap<>();
        recebimento.put("parcelaId", parcelaId);
        recebimento.put("valorRecebido", "50.00");
        recebimento.put("multa", "1.00");
        enviar(post("/api/contas-receber/" + conta.get("id").asText() + "/recebimentos"), recebimento, 201);

        JsonNode atualizada = enviar(get("/api/contas-receber/" + conta.get("id").asText()), null, 200);
        String recebimentoId = atualizada.get("parcelas").get(0).get("recebimentos").get(0).get("id").asText();
        JsonNode realizada = movimentacaoDaReferencia("CONTA_RECEBER", recebimentoId);
        org.assertj.core.api.Assertions.assertThat(realizada.get("situacao").asText()).isEqualTo("REALIZADO");
        org.assertj.core.api.Assertions.assertThat(realizada.get("valor").decimalValue())
                .isEqualByComparingTo("51.00");
        org.assertj.core.api.Assertions.assertThat(
                movimentacaoDaReferencia("CONTA_RECEBER", parcelaId).get("cancelada").asBoolean()).isTrue();
    }

    @Test
    void cancelarVendaCancelaContaReceberEProjecoes() throws Exception {
        String vendaId = criarVendaConcluida("À vista", "F");
        JsonNode conta = contaReceberDaVenda(vendaId);

        enviar(patch("/api/vendas/" + vendaId + "/cancelar"), null, 200);

        JsonNode cancelada = enviar(get("/api/contas-receber/" + conta.get("id").asText()), null, 200);
        org.assertj.core.api.Assertions.assertThat(cancelada.get("status").asText()).isEqualTo("CANCELADA");
        for (JsonNode parcela : conta.get("parcelas")) {
            org.assertj.core.api.Assertions.assertThat(
                    movimentacaoDaReferencia("CONTA_RECEBER", parcela.get("id").asText()).get("cancelada")
                            .asBoolean()).isTrue();
        }
    }

    @Test
    void naoCancelaVendaCujaContaJaTemRecebimento() throws Exception {
        String vendaId = criarVendaConcluida("À vista", "G");
        JsonNode conta = contaReceberDaVenda(vendaId);

        Map<String, Object> recebimento = new LinkedHashMap<>();
        recebimento.put("parcelaId", conta.get("parcelas").get(0).get("id").asText());
        recebimento.put("valorRecebido", "10.00");
        enviar(post("/api/contas-receber/" + conta.get("id").asText() + "/recebimentos"), recebimento, 201);

        mockMvc.perform(patch("/api/vendas/" + vendaId + "/cancelar"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/vendas/" + vendaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    // ---- Ajustes e validações dos fluxos financeiros integrados ----

    private JsonNode criarContaPagarManual(String descricao, String valor) throws Exception {
        String categoria = criarId("/api/categorias-despesa", Map.of("nome", "Cat " + descricao));
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("descricao", descricao);
        corpo.put("categoriaDespesaId", categoria);
        corpo.put("valorTotal", valor);
        corpo.put("vencimento", LocalDate.now().plusDays(15).toString());
        return enviar(post("/api/contas-pagar"), corpo, 201);
    }

    private void pagar(JsonNode conta, String valorPago, String desconto) throws Exception {
        Map<String, Object> pagamento = new LinkedHashMap<>();
        pagamento.put("parcelaId", conta.get("parcelas").get(0).get("id").asText());
        pagamento.put("valorPago", valorPago);
        if (desconto != null) {
            pagamento.put("desconto", desconto);
        }
        enviar(post("/api/contas-pagar/" + conta.get("id").asText() + "/pagamentos"), pagamento, 201);
    }

    private List<JsonNode> movimentacoesDaConta(String origem, String prefixoDescricao) throws Exception {
        List<JsonNode> doConta = new java.util.ArrayList<>();
        for (JsonNode movimentacao : movimentacoes(origem)) {
            if (movimentacao.get("descricao").asText().startsWith(prefixoDescricao)) {
                doConta.add(movimentacao);
            }
        }
        return doConta;
    }

    private java.math.BigDecimal somar(List<JsonNode> movimentacoes, String situacao, boolean incluirCanceladas) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (JsonNode movimentacao : movimentacoes) {
            boolean cancelada = movimentacao.get("cancelada").asBoolean();
            if (movimentacao.get("situacao").asText().equals(situacao) && (incluirCanceladas || !cancelada)) {
                total = total.add(movimentacao.get("valor").decimalValue());
            }
        }
        return total;
    }

    /** O caixa deve refletir a conta: previsto vigente = saldo pendente e realizado = total desembolsado. */
    private void conferirCaixaDaConta(String contaId, String origem, String prefixo) throws Exception {
        boolean pagar = origem.equals("CONTA_PAGAR");
        JsonNode conta = enviar(get((pagar ? "/api/contas-pagar/" : "/api/contas-receber/") + contaId),
                null, 200);
        java.math.BigDecimal liquidado = java.math.BigDecimal.ZERO;
        for (JsonNode parcela : conta.get("parcelas")) {
            for (JsonNode lancamento : parcela.get(pagar ? "pagamentos" : "recebimentos")) {
                liquidado = liquidado.add(lancamento.get(pagar ? "valorDesembolsado" : "valorTotalRecebido")
                        .decimalValue());
            }
        }
        List<JsonNode> doConta = movimentacoesDaConta(origem, prefixo);

        org.assertj.core.api.Assertions.assertThat(somar(doConta, "PREVISTO", false))
                .isEqualByComparingTo(conta.get("saldoPendente").decimalValue());
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "REALIZADO", false))
                .isEqualByComparingTo(liquidado);
    }

    @Test
    void descontoQuitaAParcelaSemEntrarNoCaixa() throws Exception {
        JsonNode conta = criarContaPagarManual("Conta com desconto", "100.00");

        pagar(conta, "90.00", "10.00");

        JsonNode atualizada = enviar(get("/api/contas-pagar/" + conta.get("id").asText()), null, 200);
        org.assertj.core.api.Assertions.assertThat(atualizada.get("status").asText()).isEqualTo("PAGA");
        List<JsonNode> doConta = movimentacoesDaConta("CONTA_PAGAR", "Conta a pagar: Conta com desconto (");
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "REALIZADO", false))
                .isEqualByComparingTo("90.00");
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "PREVISTO", false)).isEqualByComparingTo("0");
        conferirCaixaDaConta(conta.get("id").asText(), "CONTA_PAGAR", "Conta a pagar: Conta com desconto (");
    }

    @Test
    void parcelaQuitadaEmDoisPagamentosNaoDeixaPrevisaoSobrando() throws Exception {
        JsonNode conta = criarContaPagarManual("Conta em duas vezes", "100.00");
        String prefixo = "Conta a pagar: Conta em duas vezes (";

        pagar(conta, "30.00", null);
        conferirCaixaDaConta(conta.get("id").asText(), "CONTA_PAGAR", prefixo);
        pagar(conta, "70.00", null);

        JsonNode atualizada = enviar(get("/api/contas-pagar/" + conta.get("id").asText()), null, 200);
        org.assertj.core.api.Assertions.assertThat(atualizada.get("status").asText()).isEqualTo("PAGA");
        List<JsonNode> doConta = movimentacoesDaConta("CONTA_PAGAR", prefixo);
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "PREVISTO", false)).isEqualByComparingTo("0");
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "REALIZADO", false))
                .isEqualByComparingTo("100.00");
        conferirCaixaDaConta(conta.get("id").asText(), "CONTA_PAGAR", prefixo);
    }

    @Test
    void contaAnteriorAIntegracaoSemPrevisaoAceitaPagamento() throws Exception {
        JsonNode conta = criarContaPagarManual("Conta antiga sem previsao", "100.00");
        String parcelaId = conta.get("parcelas").get(0).get("id").asText();
        jdbcTemplate.update("DELETE FROM bella.movimentacoes_caixa WHERE origem_referencia_id = ?::uuid",
                parcelaId);

        pagar(conta, "40.00", null);

        String prefixo = "Conta a pagar: Conta antiga sem previsao (";
        List<JsonNode> doConta = movimentacoesDaConta("CONTA_PAGAR", prefixo);
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "REALIZADO", false)).isEqualByComparingTo("40.00");
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "PREVISTO", false)).isEqualByComparingTo("60.00");
        conferirCaixaDaConta(conta.get("id").asText(), "CONTA_PAGAR", prefixo);
    }

    @Test
    void recebimentoComJurosEMultaConferemComOCaixa() throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("descricao", "Receita avulsa conferida");
        corpo.put("valorTotal", "200.00");
        corpo.put("parcelas", List.of(
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(5).toString()),
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(35).toString())));
        JsonNode conta = enviar(post("/api/contas-receber"), corpo, 201);
        String contaId = conta.get("id").asText();

        Map<String, Object> recebimento = new LinkedHashMap<>();
        recebimento.put("parcelaId", conta.get("parcelas").get(0).get("id").asText());
        recebimento.put("valorRecebido", "60.00");
        recebimento.put("juros", "3.00");
        recebimento.put("multa", "2.00");
        enviar(post("/api/contas-receber/" + contaId + "/recebimentos"), recebimento, 201);

        conferirCaixaDaConta(contaId, "CONTA_RECEBER", "Conta a receber: Receita avulsa conferida (");
        List<JsonNode> doConta = movimentacoesDaConta("CONTA_RECEBER", "Conta a receber: Receita avulsa conferida (");
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "REALIZADO", false))
                .isEqualByComparingTo("65.00");
        org.assertj.core.api.Assertions.assertThat(somar(doConta, "PREVISTO", false)).isEqualByComparingTo("140.00");
    }

    @Test
    void cancelarVendaComContaJaCanceladaManualmenteFunciona() throws Exception {
        String vendaId = criarVendaConcluida("À vista", "H");
        JsonNode conta = contaReceberDaVenda(vendaId);
        enviar(patch("/api/contas-receber/" + conta.get("id").asText() + "/cancelar"),
                Map.of("motivo", "Cancelada manualmente"), 200);

        JsonNode venda = enviar(patch("/api/vendas/" + vendaId + "/cancelar"), null, 200);

        org.assertj.core.api.Assertions.assertThat(venda.get("status").asText()).isEqualTo("CANCELADA");
        org.assertj.core.api.Assertions.assertThat(
                enviar(get("/api/contas-receber/" + conta.get("id").asText()), null, 200).get("status").asText())
                .isEqualTo("CANCELADA");
    }

    @Test
    void contasGeradasPorCompraEVendaNaoTemValorNemParcelasAlterados() throws Exception {
        JsonNode contaDaCompra = contaPagarDaCompra(criarCompraRecebida("30 dias", "J"));
        Map<String, Object> edicaoPagar = new LinkedHashMap<>();
        edicaoPagar.put("descricao", "Nova descricao");
        edicaoPagar.put("categoriaDespesaId", contaDaCompra.get("categoriaDespesaId").asText());
        edicaoPagar.put("valorTotal", "999.00");
        edicaoPagar.put("vencimento", LocalDate.now().plusDays(10).toString());
        enviar(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/contas-pagar/" + contaDaCompra.get("id").asText()), edicaoPagar, 422);

        Map<String, Object> dadosBasicos = new LinkedHashMap<>();
        dadosBasicos.put("descricao", "Descricao corrigida");
        dadosBasicos.put("fornecedorId", contaDaCompra.get("fornecedorId").asText());
        dadosBasicos.put("categoriaDespesaId", contaDaCompra.get("categoriaDespesaId").asText());
        JsonNode corrigida = enviar(patch("/api/contas-pagar/" + contaDaCompra.get("id").asText()
                + "/dados-basicos"), dadosBasicos, 200);
        org.assertj.core.api.Assertions.assertThat(corrigida.get("descricao").asText())
                .isEqualTo("Descricao corrigida");

        String vendaId = criarVendaConcluida("À vista", "I");
        JsonNode contaDaVenda = contaReceberDaVenda(vendaId);
        Map<String, Object> edicaoReceber = new LinkedHashMap<>();
        edicaoReceber.put("descricao", "Nova descricao");
        edicaoReceber.put("clienteId", contaDaVenda.get("clienteId").asText());
        edicaoReceber.put("valorTotal", "1.00");
        edicaoReceber.put("vencimento", LocalDate.now().plusDays(10).toString());
        enviar(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/contas-receber/" + contaDaVenda.get("id").asText()), edicaoReceber, 422);
    }

    @Test
    void categoriaDoSistemaNaoPodeSerInativadaNemRenomeadaMasContinuaAceitandoCompras() throws Exception {
        String categoriaDoSistema = "00000000-0000-0000-0000-0000000c0001";

        enviar(patch("/api/categorias-despesa/" + categoriaDoSistema + "/status"), Map.of("status", "INATIVO"), 422);
        enviar(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .put("/api/categorias-despesa/" + categoriaDoSistema), Map.of("nome", "Outro nome"), 422);

        JsonNode categoria = enviar(get("/api/categorias-despesa/" + categoriaDoSistema), null, 200);
        org.assertj.core.api.Assertions.assertThat(categoria.get("nome").asText()).isEqualTo("Compra de mercadorias");
        org.assertj.core.api.Assertions.assertThat(categoria.get("status").asText()).isEqualTo("ATIVO");
        org.assertj.core.api.Assertions.assertThat(contaPagarDaCompra(criarCompraRecebida("avista", "K")))
                .isNotNull();
    }

    @Test
    void migracaoPreencheAsPrevisoesQueFaltamExatamenteComoOSistemaCria() throws Exception {
        criarContaPagarManual("Migracao sem pagamento", "100.00");
        JsonNode parcial = criarContaPagarManual("Migracao parcial", "100.00");
        pagar(parcial, "25.00", "5.00");
        JsonNode quitada = criarContaPagarManual("Migracao quitada", "50.00");
        pagar(quitada, "50.00", null);
        JsonNode cancelada = criarContaPagarManual("Migracao cancelada", "70.00");
        enviar(patch("/api/contas-pagar/" + cancelada.get("id").asText() + "/cancelar"),
                Map.of("motivo", "Teste"), 200);
        Map<String, Object> receita = new LinkedHashMap<>();
        receita.put("descricao", "Migracao receita " + "x".repeat(178));
        receita.put("valorTotal", "300.00");
        receita.put("parcelas", List.of(
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(5).toString()),
                Map.of("valor", "200.00", "vencimento", LocalDate.now().plusDays(35).toString())));
        JsonNode contaReceber = enviar(post("/api/contas-receber"), receita, 201);
        Map<String, Object> recebimento = new LinkedHashMap<>();
        recebimento.put("parcelaId", contaReceber.get("parcelas").get(1).get("id").asText());
        recebimento.put("valorRecebido", "80.00");
        enviar(post("/api/contas-receber/" + contaReceber.get("id").asText() + "/recebimentos"), recebimento, 201);

        Map<String, String[]> esperado = previsoesVigentes();
        org.assertj.core.api.Assertions.assertThat(esperado).isNotEmpty();

        jdbcTemplate.update("DELETE FROM bella.movimentacoes_caixa WHERE situacao = 'PREVISTO'"
                + " AND origem IN ('CONTA_PAGAR', 'CONTA_RECEBER')");
        org.assertj.core.api.Assertions.assertThat(previsoesVigentes()).isEmpty();

        String migracao = new String(new org.springframework.core.io.ClassPathResource(
                "db/migration/V33__preencher_previsoes_caixa_contas_antigas.sql").getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        jdbcTemplate.execute(migracao);

        Map<String, String[]> preenchido = previsoesVigentes();
        org.assertj.core.api.Assertions.assertThat(preenchido.keySet()).containsExactlyInAnyOrderElementsOf(
                esperado.keySet());
        for (Map.Entry<String, String[]> entrada : esperado.entrySet()) {
            String[] obtido = preenchido.get(entrada.getKey());
            org.assertj.core.api.Assertions.assertThat(new java.math.BigDecimal(obtido[0]))
                    .isEqualByComparingTo(new java.math.BigDecimal(entrada.getValue()[0]));
            org.assertj.core.api.Assertions.assertThat(obtido[1]).isEqualTo(entrada.getValue()[1]);
            org.assertj.core.api.Assertions.assertThat(obtido[2]).isEqualTo(entrada.getValue()[2]);
            org.assertj.core.api.Assertions.assertThat(obtido[3]).hasSizeLessThanOrEqualTo(200);
        }

        jdbcTemplate.execute(migracao);
        org.assertj.core.api.Assertions.assertThat(previsoesVigentes()).hasSameSizeAs(esperado);
    }

    /** Previsões vigentes das contas, indexadas pela referência de origem: valor, data, tipo e descrição. */
    private Map<String, String[]> previsoesVigentes() throws Exception {
        Map<String, String[]> resultado = new LinkedHashMap<>();
        for (String origem : List.of("CONTA_PAGAR", "CONTA_RECEBER")) {
            for (JsonNode movimentacao : enviar(get("/api/fluxo-caixa/movimentacoes").param("origem", origem)
                    .param("situacao", "PREVISTO"), null, 200)) {
                resultado.put(movimentacao.get("origemReferenciaId").asText(), new String[] {
                        movimentacao.get("valor").asText(), movimentacao.get("dataPrevista").asText(),
                        movimentacao.get("tipo").asText(), movimentacao.get("descricao").asText()});
            }
        }
        return resultado;
    }
}
