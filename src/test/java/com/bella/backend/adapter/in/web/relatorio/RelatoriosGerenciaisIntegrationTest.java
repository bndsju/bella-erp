package com.bella.backend.adapter.in.web.relatorio;

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

class RelatoriosGerenciaisIntegrationTest extends IntegrationTestBase {

    /** Identificadores do cenário, criado uma única vez para todos os testes da classe. */
    private record Cenario(String maria, String joao, String produtoA, String produtoB, String produtoC,
                           String categoriaB, String categoriaDespesa) {
    }

    private static Cenario cenario;

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

    private String criarCliente(String nome, String cpf, String email) throws Exception {
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
        cliente.put("celular", "11999998888");
        cliente.put("email", email);
        cliente.put("endereco", endereco);
        return criarId("/api/clientes", cliente);
    }

    private String criarProduto(String sufixo, String estoqueMinimo, String quantidadeInicial) throws Exception {
        String categoriaId = criarId("/api/categorias", Map.of("nome", "Categoria " + sufixo));
        Map<String, Object> unidade = new LinkedHashMap<>();
        unidade.put("nome", "Unidade " + sufixo);
        unidade.put("sigla", "un");
        String unidadeId = criarId("/api/unidades-medida", unidade);
        Map<String, Object> produto = new LinkedHashMap<>();
        produto.put("codigoInterno", "SKU-REL-" + sufixo);
        produto.put("nome", "Produto " + sufixo);
        produto.put("categoriaId", categoriaId);
        produto.put("unidadeMedidaId", unidadeId);
        produto.put("precoCusto", "30.00");
        produto.put("precoVenda", "50.00");
        produto.put("estoqueMinimo", estoqueMinimo);
        String produtoId = criarId("/api/produtos", produto);

        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("produtoId", produtoId);
        entrada.put("quantidade", quantidadeInicial);
        entrada.put("motivo", "Estoque inicial");
        criarId("/api/estoque/entradas", entrada);
        return produtoId;
    }

    private String categoriaDoProduto(String produtoId) throws Exception {
        return enviar(get("/api/produtos/" + produtoId), null, 200).get("categoria").get("id").asText();
    }

    private Map<String, Object> corpoPedido(String clienteId, String produtoId, String quantidade, String valor) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", quantidade);
        item.put("valorUnitario", valor);
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("clienteId", clienteId);
        pedido.put("itens", List.of(item));
        pedido.put("percentualDesconto", "0");
        pedido.put("valorFrete", "0");
        pedido.put("condicaoPagamento", "À vista");
        return pedido;
    }

    private void entregar(String pedidoId) throws Exception {
        for (String passo : List.of("confirmar", "iniciar-separacao", "marcar-pronto-para-entrega", "entregar")) {
            enviar(patch("/api/pedidos/" + pedidoId + "/" + passo), null, 200);
        }
    }

    private String vender(String clienteId, String produtoId, String quantidade, String valor) throws Exception {
        String pedidoId = criarId("/api/pedidos", corpoPedido(clienteId, produtoId, quantidade, valor));
        entregar(pedidoId);
        return criarId("/api/vendas", Map.of("pedidoId", pedidoId, "formaPagamento", "PIX"));
    }

    private String criarOrcamento(String clienteId, String produtoId, String valorUnitario) throws Exception {
        Map<String, Object> corpo = corpoPedido(clienteId, produtoId, "2", valorUnitario);
        corpo.put("prazoValidade", LocalDate.now().plusDays(7).toString());
        return criarId("/api/orcamentos", corpo);
    }

    /** Monta o cenário uma única vez; o banco é compartilhado pelos testes da classe. */
    private synchronized Cenario cenario() throws Exception {
        if (cenario != null) {
            return cenario;
        }
        String maria = criarCliente("Maria Relatorio", "111.444.777-35", "maria-rel@example.com");
        String joao = criarCliente("Joao Relatorio", "529.982.247-25", "joao-rel@example.com");
        String produtoA = criarProduto("A", "5", "100");
        String produtoB = criarProduto("B", "5", "100");
        String produtoC = criarProduto("C", "10", "5");

        // Maria: orçamento aprovado -> pedido -> entregue -> venda (2 x 50 = 100)
        String orcamento = criarOrcamento(maria, produtoA, "50.00");
        enviar(patch("/api/orcamentos/" + orcamento + "/enviar"), null, 200);
        enviar(patch("/api/orcamentos/" + orcamento + "/aprovar"), null, 200);
        String pedidoMaria = enviar(post("/api/pedidos/a-partir-de-orcamento"), Map.of("orcamentoId", orcamento),
                201).get("id").asText();
        entregar(pedidoMaria);
        criarId("/api/vendas", Map.of("pedidoId", pedidoMaria, "formaPagamento", "PIX"));

        // outro orçamento emitido, sem andamento
        String parado = criarOrcamento(maria, produtoA, "20.00");
        enviar(patch("/api/orcamentos/" + parado + "/enviar"), null, 200);

        // João: uma venda concluída (1 x 30) e outra cancelada, que não pode contar
        vender(joao, produtoB, "1", "30.00");
        String vendaCancelada = vender(joao, produtoB, "1", "30.00");
        enviar(patch("/api/vendas/" + vendaCancelada + "/cancelar"), null, 200);

        // pedido cancelado
        String pedidoCancelado = criarId("/api/pedidos", corpoPedido(joao, produtoB, "1", "30.00"));
        enviar(patch("/api/pedidos/" + pedidoCancelado + "/cancelar"), Map.of("motivo", "Desistência"), 200);

        // financeiro: conta vencida, conta parcelada futura e aporte realizado
        String categoriaDespesa = criarId("/api/categorias-despesa", Map.of("nome", "Aluguel REL"));
        Map<String, Object> vencida = new LinkedHashMap<>();
        vencida.put("descricao", "Aluguel atrasado");
        vencida.put("categoriaDespesaId", categoriaDespesa);
        vencida.put("valorTotal", "300.00");
        vencida.put("vencimento", LocalDate.now().minusDays(1).toString());
        criarId("/api/contas-pagar", vencida);

        Map<String, Object> parcelada = new LinkedHashMap<>();
        parcelada.put("descricao", "Equipamento");
        parcelada.put("categoriaDespesaId", categoriaDespesa);
        parcelada.put("valorTotal", "200.00");
        parcelada.put("parcelas", List.of(
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(30).toString()),
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(60).toString())));
        criarId("/api/contas-pagar", parcelada);

        String categoriaCaixa = criarId("/api/categorias-fluxo-caixa", Map.of("nome", "Aportes REL"));
        Map<String, Object> aporte = new LinkedHashMap<>();
        aporte.put("tipo", "ENTRADA");
        aporte.put("descricao", "Aporte");
        aporte.put("categoriaId", categoriaCaixa);
        aporte.put("valor", "500.00");
        aporte.put("situacao", "REALIZADO");
        aporte.put("dataPrevista", LocalDate.now().toString());
        aporte.put("dataEfetiva", LocalDate.now().toString());
        criarId("/api/fluxo-caixa/movimentacoes", aporte);

        cenario = new Cenario(maria, joao, produtoA, produtoB, produtoC, categoriaDoProduto(produtoB),
                categoriaDespesa);
        return cenario;
    }

    private JsonNode relatorio(String caminho, String... parametros) throws Exception {
        cenario();
        MockHttpServletRequestBuilder requisicao = get("/api/relatorios/" + caminho);
        for (int i = 0; i < parametros.length; i += 2) {
            requisicao = requisicao.param(parametros[i], parametros[i + 1]);
        }
        return enviar(requisicao, null, 200);
    }

    private void relatorioInvalido(int statusEsperado, String caminho, String... parametros) throws Exception {
        cenario();
        MockHttpServletRequestBuilder requisicao = get("/api/relatorios/" + caminho);
        for (int i = 0; i < parametros.length; i += 2) {
            requisicao = requisicao.param(parametros[i], parametros[i + 1]);
        }
        mockMvc.perform(requisicao).andExpect(status().is(statusEsperado));
    }

    @Test
    void relatorioDeVendasConsideraApenasVendasConcluidasEAplicaFiltros() throws Exception {
        Cenario c = cenario();

        JsonNode todas = relatorio("vendas");
        assertThat(todas.get("resumo").get("quantidade").asInt()).isEqualTo(2);
        assertThat(todas.get("resumo").get("faturamento").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(todas.get("resumo").get("ticketMedio").decimalValue()).isEqualByComparingTo("65.00");
        assertThat(todas.get("vendas").get("totalItens").asInt()).isEqualTo(2);

        JsonNode daMaria = relatorio("vendas", "clienteId", c.maria());
        assertThat(daMaria.get("resumo").get("quantidade").asInt()).isEqualTo(1);
        assertThat(daMaria.get("resumo").get("faturamento").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(daMaria.get("vendas").get("itens").get(0).get("clienteNome").asText())
                .isEqualTo("Maria Relatorio");

        JsonNode doProdutoB = relatorio("vendas", "produtoId", c.produtoB());
        assertThat(doProdutoB.get("resumo").get("quantidade").asInt()).isEqualTo(1);

        JsonNode pagina = relatorio("vendas", "tamanho", "1", "pagina", "1");
        assertThat(pagina.get("vendas").get("itens")).hasSize(1);
        assertThat(pagina.get("vendas").get("totalPaginas").asInt()).isEqualTo(2);

        double faturamentoDaSerie = 0;
        for (JsonNode ponto : todas.get("serie")) {
            faturamentoDaSerie += ponto.get("valor").asDouble();
        }
        assertThat(faturamentoDaSerie).isEqualTo(130.0);
    }

    @Test
    void periodoSemVendasRetornaZerosESerieCompleta() throws Exception {
        JsonNode vazio = relatorio("vendas", "inicio", "2000-01-01", "fim", "2000-01-05");

        assertThat(vazio.get("resumo").get("quantidade").asInt()).isZero();
        assertThat(vazio.get("resumo").get("ticketMedio").decimalValue()).isEqualByComparingTo("0");
        assertThat(vazio.get("serie")).hasSize(5);
        assertThat(vazio.get("vendas").get("itens")).isEmpty();
        assertThat(vazio.get("vendas").get("totalPaginas").asInt()).isZero();
    }

    @Test
    void pedidosPorStatusSeparaCanceladosDosValidos() throws Exception {
        JsonNode resultado = relatorio("pedidos-por-status");

        assertThat(resultado.get("porStatus")).hasSize(6);
        int entregues = 0;
        int cancelados = 0;
        for (JsonNode linha : resultado.get("porStatus")) {
            if ("ENTREGUE".equals(linha.get("status").asText())) {
                entregues = linha.get("quantidade").asInt();
            }
            if ("CANCELADO".equals(linha.get("status").asText())) {
                cancelados = linha.get("quantidade").asInt();
            }
        }
        assertThat(entregues).isEqualTo(3);
        assertThat(cancelados).isEqualTo(1);
        assertThat(resultado.get("quantidadeValidos").asInt()).isEqualTo(3);
    }

    @Test
    void orcamentosMostramEmitidosAprovadosETaxas() throws Exception {
        JsonNode resultado = relatorio("orcamentos");

        assertThat(resultado.get("emitidos").asInt()).isEqualTo(2);
        assertThat(resultado.get("aprovados").asInt()).isEqualTo(1);
        assertThat(resultado.get("recusados").asInt()).isZero();
        assertThat(resultado.get("valorEmitidos").decimalValue()).isEqualByComparingTo("140.00");
        assertThat(resultado.get("taxaAprovacao").decimalValue()).isEqualByComparingTo("50.0");
        assertThat(resultado.get("taxaConversaoPedidos").decimalValue()).isEqualByComparingTo("50.0");
        assertThat(resultado.get("taxaConversaoVendas").decimalValue()).isEqualByComparingTo("50.0");
        assertThat(resultado.get("porStatus")).hasSize(6);
    }

    @Test
    void orcamentosSemEmitidosNaoTemTaxa() throws Exception {
        JsonNode vazio = relatorio("orcamentos", "inicio", "2000-01-01", "fim", "2000-01-31");

        assertThat(vazio.get("emitidos").asInt()).isZero();
        assertThat(vazio.get("taxaConversaoPedidos").isNull()).isTrue();
        assertThat(vazio.get("taxaAprovacao").isNull()).isTrue();
    }

    @Test
    void produtosMaisVendidosRespeitaFiltroDeCategoriaELimite() throws Exception {
        Cenario c = cenario();

        JsonNode todos = relatorio("produtos-mais-vendidos");
        assertThat(todos.get("produtos")).hasSize(2);
        assertThat(todos.get("produtos").get(0).get("nome").asText()).isEqualTo("Produto A");
        assertThat(todos.get("produtos").get(0).get("quantidade").decimalValue()).isEqualByComparingTo("2");
        assertThat(todos.get("produtos").get(0).get("categoriaNome").asText()).isEqualTo("Categoria A");

        JsonNode daCategoria = relatorio("produtos-mais-vendidos", "categoriaId", c.categoriaB());
        assertThat(daCategoria.get("produtos")).hasSize(1);
        assertThat(daCategoria.get("produtos").get(0).get("nome").asText()).isEqualTo("Produto B");

        assertThat(relatorio("produtos-mais-vendidos", "limite", "1").get("produtos")).hasSize(1);
    }

    @Test
    void clientesComMaiorVolumeTemParticipacaoSobreOFaturamento() throws Exception {
        JsonNode resultado = relatorio("clientes-maior-volume");

        assertThat(resultado.get("faturamentoTotal").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(resultado.get("clientes")).hasSize(2);
        JsonNode primeiro = resultado.get("clientes").get(0);
        assertThat(primeiro.get("nome").asText()).isEqualTo("Maria Relatorio");
        assertThat(primeiro.get("valor").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(primeiro.get("participacaoPercentual").decimalValue()).isEqualByComparingTo("76.9");
        assertThat(resultado.get("clientes").get(1).get("nome").asText()).isEqualTo("Joao Relatorio");
    }

    @Test
    void relatoriosDeEstoqueMostramMovimentacoesEProdutosEmFalta() throws Exception {
        Cenario c = cenario();

        JsonNode movimentacoes = relatorio("estoque/movimentacoes", "produtoId", c.produtoA());
        boolean achouEntrada = false;
        for (JsonNode total : movimentacoes.get("totaisPorTipo")) {
            if ("ENTRADA".equals(total.get("tipo").asText())) {
                achouEntrada = true;
                assertThat(total.get("quantidadeTotal").decimalValue()).isEqualByComparingTo("100");
            }
        }
        assertThat(achouEntrada).isTrue();
        assertThat(movimentacoes.get("movimentacoes").get("totalItens").asInt()).isGreaterThanOrEqualTo(1);

        JsonNode soEntradas = relatorio("estoque/movimentacoes", "tipo", "ENTRADA");
        for (JsonNode linha : soEntradas.get("movimentacoes").get("itens")) {
            assertThat(linha.get("tipo").asText()).isEqualTo("ENTRADA");
        }

        JsonNode posicao = relatorio("estoque/posicao");
        assertThat(posicao.get("totalProdutos").asInt()).isEqualTo(3);
        assertThat(posicao.get("produtosAbaixoDoMinimo").asInt()).isEqualTo(1);

        JsonNode abaixo = relatorio("estoque/abaixo-do-minimo");
        assertThat(abaixo.get("produtos").get("itens")).hasSize(1);
        assertThat(abaixo.get("produtos").get("itens").get(0).get("nome").asText()).isEqualTo("Produto C");
        assertThat(abaixo.get("produtos").get("itens").get(0).get("abaixoDoMinimo").asBoolean()).isTrue();

        JsonNode daCategoria = relatorio("estoque/posicao", "categoriaId", categoriaDoProduto(c.produtoA()));
        assertThat(daCategoria.get("totalProdutos").asInt()).isEqualTo(1);
    }

    @Test
    void contasAReceberExcluemCanceladasENaoMisturamValores() throws Exception {
        JsonNode resultado = relatorio("contas-receber");

        assertThat(resultado.get("totais").get("quantidadeContas").asInt()).isEqualTo(2);
        assertThat(resultado.get("totais").get("valorTotal").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(resultado.get("totais").get("saldoPendente").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(resultado.get("totais").get("valorLiquidado").decimalValue()).isEqualByComparingTo("0");
        assertThat(resultado.get("contas").get("itens").get(0).get("parceiroNome").asText()).isNotBlank();

        JsonNode canceladas = relatorio("contas-receber", "status", "CANCELADA");
        assertThat(canceladas.get("totais").get("quantidadeContas").asInt()).isEqualTo(1);
    }

    @Test
    void contasAPagarSomamVencidoEAVencerPorCategoria() throws Exception {
        Cenario c = cenario();

        JsonNode resultado = relatorio("contas-pagar", "categoriaDespesaId", c.categoriaDespesa());
        assertThat(resultado.get("totais").get("quantidadeContas").asInt()).isEqualTo(2);
        assertThat(resultado.get("totais").get("saldoPendente").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(resultado.get("totais").get("saldoVencido").decimalValue()).isEqualByComparingTo("300.00");
        assertThat(resultado.get("totais").get("saldoAVencer").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(resultado.get("contas").get("itens").get(0).get("categoriaNome").asText())
                .isEqualTo("Aluguel REL");

        JsonNode soVencidas = relatorio("contas-pagar", "situacao", "VENCIDAS");
        assertThat(soVencidas.get("totais").get("quantidadeContas").asInt()).isEqualTo(1);

        String proximoMes = LocalDate.now().plusDays(25).toString();
        JsonNode porVencimento = relatorio("contas-pagar", "inicio", proximoMes, "fim",
                LocalDate.now().plusDays(35).toString());
        assertThat(porVencimento.get("totais").get("quantidadeContas").asInt()).isEqualTo(1);
    }

    @Test
    void vencimentosSeparamVencidosEAVencerPorFaixa() throws Exception {
        JsonNode resultado = relatorio("vencimentos");

        JsonNode totais = resultado.get("totais");
        assertThat(totais.get("vencidoPagar").decimalValue()).isEqualByComparingTo("300.00");
        assertThat(totais.get("aVencerPagar").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(totais.get("vencidoReceber").decimalValue()).isEqualByComparingTo("0");
        assertThat(totais.get("aVencerReceber").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(resultado.get("faixas")).hasSize(7);
        assertThat(resultado.get("titulos").get("totalItens").asInt()).isEqualTo(5);

        JsonNode faixaDeHoje = null;
        for (JsonNode faixa : resultado.get("faixas")) {
            if (faixa.get("faixa").asText().startsWith("A vencer") && faixa.get("faixa").asText().endsWith("7 dias")) {
                faixaDeHoje = faixa;
            }
        }
        assertThat(faixaDeHoje).isNotNull();
        assertThat(faixaDeHoje.get("receber").decimalValue()).isEqualByComparingTo("130.00");

        JsonNode soPagar = relatorio("vencimentos", "tipo", "PAGAR");
        assertThat(soPagar.get("titulos").get("totalItens").asInt()).isEqualTo(3);

        JsonNode soVencidos = relatorio("vencimentos", "apenasVencidos", "true");
        assertThat(soVencidos.get("titulos").get("totalItens").asInt()).isEqualTo(1);
        assertThat(soVencidos.get("titulos").get("itens").get(0).get("diasEmAtraso").asLong()).isEqualTo(1);
    }

    @Test
    void fluxoDeCaixaSeparaPrevistoERealizadoEAplicaFiltros() throws Exception {
        String inicio = LocalDate.now().minusDays(40).toString();
        String fim = LocalDate.now().plusDays(70).toString();

        JsonNode resultado = relatorio("fluxo-caixa", "inicio", inicio, "fim", fim);
        JsonNode resumo = resultado.get("resumo");
        assertThat(resumo.get("entradasRealizadas").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(resumo.get("entradasPrevistas").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(resumo.get("saidasPrevistas").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(resumo.get("saldoRealizado").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(resumo.get("saldoProjetado").decimalValue()).isEqualByComparingTo("130.00");
        assertThat(resultado.get("granularidade").asText()).isEqualTo("MES");
        assertThat(resultado.get("saldoAcumuladoAteFim").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(resultado.get("porCategoria").size()).isGreaterThanOrEqualTo(2);

        JsonNode soSaidas = relatorio("fluxo-caixa", "inicio", inicio, "fim", fim, "tipo", "SAIDA");
        assertThat(soSaidas.get("resumo").get("entradasRealizadas").decimalValue()).isEqualByComparingTo("0");
        assertThat(soSaidas.get("movimentacoes").get("totalItens").asInt()).isEqualTo(3);

        JsonNode soRealizado = relatorio("fluxo-caixa", "inicio", inicio, "fim", fim, "situacao", "REALIZADO");
        assertThat(soRealizado.get("resumo").get("saidasPrevistas").decimalValue()).isEqualByComparingTo("0");
        assertThat(soRealizado.get("movimentacoes").get("totalItens").asInt()).isEqualTo(1);

        JsonNode porOrigem = relatorio("fluxo-caixa", "inicio", inicio, "fim", fim, "origem", "CONTA_RECEBER");
        assertThat(porOrigem.get("resumo").get("entradasPrevistas").decimalValue()).isEqualByComparingTo("130.00");
    }

    @Test
    void validaParametrosDeConsulta() throws Exception {
        relatorioInvalido(422, "vendas", "inicio", "2026-02-10", "fim", "2026-02-01");
        relatorioInvalido(422, "contas-pagar", "inicio", "2026-02-10", "fim", "2026-02-01");
        relatorioInvalido(422, "vendas", "tamanho", "0");
        relatorioInvalido(422, "vendas", "tamanho", "201");
        relatorioInvalido(422, "fluxo-caixa", "pagina", "-1");
        relatorioInvalido(422, "produtos-mais-vendidos", "limite", "0");
        relatorioInvalido(422, "clientes-maior-volume", "limite", "101");
        relatorioInvalido(400, "vendas", "formaPagamento", "FIADO");
        relatorioInvalido(400, "vencimentos", "inicio", "10/02/2026");
    }

    @Test
    void consultasNaoAlteramOsDados() throws Exception {
        String antes = enviar(get("/api/fluxo-caixa/resumo"), null, 200).toString();
        relatorio("fluxo-caixa");
        relatorio("vencimentos");
        relatorio("contas-receber");
        String depois = enviar(get("/api/fluxo-caixa/resumo"), null, 200).toString();

        assertThat(depois).isEqualTo(antes);
    }
}
