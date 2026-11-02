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
        String compraId = criarCompraRecebida("30 dias", "D");
        JsonNode conta = contaPagarDaCompra(compraId);
        String parcelaAntiga = conta.get("parcelas").get(0).get("id").asText();

        Map<String, Object> edicao = new LinkedHashMap<>();
        edicao.put("descricao", conta.get("descricao").asText());
        edicao.put("fornecedorId", conta.get("fornecedorId").asText());
        edicao.put("categoriaDespesaId", conta.get("categoriaDespesaId").asText());
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
        cliente.put("cpf", Map.of("E", "111.444.777-35", "F", "390.533.447-05", "G", "529.982.247-25").get(sufixo));
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
}
