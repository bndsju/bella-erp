package com.bella.backend.adapter.in.web.estoque;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EstoqueControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String criarCliente(String nome, String cpf, String email) throws Exception {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01310-100");
        endereco.put("logradouro", "Av. Paulista");
        endereco.put("numero", "1000");
        endereco.put("bairro", "Bela Vista");
        endereco.put("cidade", "São Paulo");
        endereco.put("uf", "SP");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tipoPessoa", "PF");
        corpo.put("nomeCompleto", nome);
        corpo.put("dataNascimento", "1990-05-10");
        corpo.put("cpf", cpf);
        corpo.put("celular", "11999998888");
        corpo.put("email", email);
        corpo.put("endereco", endereco);

        String resposta = mockMvc.perform(post("/api/clientes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarCategoria(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("nome", nome))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarUnidadeMedida(String nome, String sigla) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("sigla", sigla);
        String resposta = mockMvc.perform(post("/api/unidades-medida").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarProduto(String codigoInterno, String nome, String categoriaId, String unidadeMedidaId,
                                 String estoqueMinimo) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigoInterno", codigoInterno);
        corpo.put("nome", nome);
        corpo.put("categoriaId", categoriaId);
        corpo.put("unidadeMedidaId", unidadeMedidaId);
        corpo.put("precoCusto", "30.00");
        corpo.put("precoVenda", "50.00");
        corpo.put("estoqueMinimo", estoqueMinimo);
        String resposta = mockMvc.perform(post("/api/produtos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private Map<String, Object> corpoMovimentacao(String produtoId, String quantidade, String motivo) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("produtoId", produtoId);
        corpo.put("quantidade", quantidade);
        corpo.put("motivo", motivo);
        return corpo;
    }

    private String corpoPedido(String clienteId, String produtoId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "3");
        item.put("valorUnitario", "50.00");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("clienteId", clienteId);
        corpo.put("itens", List.of(item));
        corpo.put("percentualDesconto", "0");
        corpo.put("valorFrete", "0");
        corpo.put("condicaoPagamento", "À vista");
        try {
            return objectMapper.writeValueAsString(corpo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void registraEntradaEAumentaSaldo() throws Exception {
        String categoriaId = criarCategoria("Categoria Entrada");
        String unidadeId = criarUnidadeMedida("Unidade Entrada", "un");
        String produtoId = criarProduto("SKU-EST-001", "Produto Entrada", categoriaId, unidadeId, "0");

        mockMvc.perform(post("/api/estoque/entradas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpoMovimentacao(produtoId, "10", "Compra de fornecedor"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.origem").value("MANUAL"));

        mockMvc.perform(get("/api/estoque/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(10))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10));
    }

    @Test
    void rejeitaSaidaQuandoEstoqueInsuficiente() throws Exception {
        String categoriaId = criarCategoria("Categoria Saida Insuficiente");
        String unidadeId = criarUnidadeMedida("Unidade Saida Insuficiente", "un");
        String produtoId = criarProduto("SKU-EST-002", "Produto Saida", categoriaId, unidadeId, "0");

        mockMvc.perform(post("/api/estoque/saidas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoMovimentacao(produtoId, "5", "Venda balcão"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void ajusteDeEntradaESaidaAlteramSaldo() throws Exception {
        String categoriaId = criarCategoria("Categoria Ajuste");
        String unidadeId = criarUnidadeMedida("Unidade Ajuste", "un");
        String produtoId = criarProduto("SKU-EST-003", "Produto Ajuste", categoriaId, unidadeId, "0");

        mockMvc.perform(post("/api/estoque/ajustes/entrada").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpoMovimentacao(produtoId, "20", "Correção de inventário"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("AJUSTE_ENTRADA"));

        mockMvc.perform(post("/api/estoque/ajustes/saida").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoMovimentacao(produtoId, "5", "Perda/avaria"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("AJUSTE_SAIDA"));

        mockMvc.perform(get("/api/estoque/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(15));
    }

    @Test
    void listaProdutosAbaixoDoMinimo() throws Exception {
        String categoriaId = criarCategoria("Categoria Minimo");
        String unidadeId = criarUnidadeMedida("Unidade Minimo", "un");
        String produtoId = criarProduto("SKU-EST-004", "Produto Abaixo Minimo", categoriaId, unidadeId, "10");

        mockMvc.perform(post("/api/estoque/entradas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoMovimentacao(produtoId, "3", "Compra"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/estoque/abaixo-do-minimo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.produtoId == '" + produtoId + "')]").exists());
    }

    @Test
    void fluxoDePedidoReservaLiberaEConsomeEstoque() throws Exception {
        String clienteId = criarCliente("Cliente Estoque", "111.444.777-35", "estoque@example.com");
        String categoriaId = criarCategoria("Categoria Fluxo Estoque");
        String unidadeId = criarUnidadeMedida("Unidade Fluxo Estoque", "un");
        String produtoId = criarProduto("SKU-EST-005", "Produto Fluxo Estoque", categoriaId, unidadeId, "0");

        mockMvc.perform(post("/api/estoque/entradas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoMovimentacao(produtoId, "10", "Compra"))))
                .andExpect(status().isCreated());

        String respostaPedido = mockMvc.perform(post("/api/pedidos").contentType("application/json")
                        .content(corpoPedido(clienteId, produtoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String pedidoId = objectMapper.readTree(respostaPedido).get("id").asText();

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/confirmar")).andExpect(status().isOk());

        mockMvc.perform(get("/api/estoque/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(10))
                .andExpect(jsonPath("$.quantidadeReservada").value(3))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(7));

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/iniciar-separacao")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/marcar-pronto-para-entrega")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/entregar")).andExpect(status().isOk());

        mockMvc.perform(get("/api/estoque/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(7))
                .andExpect(jsonPath("$.quantidadeReservada").value(0))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(7));

        mockMvc.perform(get("/api/estoque/movimentacoes").param("produtoId", produtoId).param("tipo", "SAIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origem").value("PEDIDO"));
    }

    @Test
    void cancelarPedidoLiberaReserva() throws Exception {
        String clienteId = criarCliente("Cliente Cancela Estoque", "112.223.334-57", "cancelaestoque@example.com");
        String categoriaId = criarCategoria("Categoria Cancela Estoque");
        String unidadeId = criarUnidadeMedida("Unidade Cancela Estoque", "un");
        String produtoId = criarProduto("SKU-EST-006", "Produto Cancela Estoque", categoriaId, unidadeId, "0");

        mockMvc.perform(post("/api/estoque/entradas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoMovimentacao(produtoId, "10", "Compra"))))
                .andExpect(status().isCreated());

        String respostaPedido = mockMvc.perform(post("/api/pedidos").contentType("application/json")
                        .content(corpoPedido(clienteId, produtoId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String pedidoId = objectMapper.readTree(respostaPedido).get("id").asText();

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/confirmar")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/cancelar")).andExpect(status().isOk());

        mockMvc.perform(get("/api/estoque/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(10))
                .andExpect(jsonPath("$.quantidadeReservada").value(0))
                .andExpect(jsonPath("$.quantidadeDisponivel").value(10));
    }
}
