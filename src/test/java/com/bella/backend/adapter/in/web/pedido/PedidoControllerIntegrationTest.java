package com.bella.backend.adapter.in.web.pedido;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PedidoControllerIntegrationTest extends IntegrationTestBase {

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

    private String criarProduto(String codigoInterno, String nome, String categoriaId, String unidadeMedidaId)
            throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigoInterno", codigoInterno);
        corpo.put("nome", nome);
        corpo.put("categoriaId", categoriaId);
        corpo.put("unidadeMedidaId", unidadeMedidaId);
        corpo.put("precoCusto", "30.00");
        corpo.put("precoVenda", "50.00");
        corpo.put("estoqueMinimo", "0");
        String resposta = mockMvc.perform(post("/api/produtos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarTransportadora(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/transportadoras").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("nome", nome))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarOrcamentoAprovado(String clienteId, String produtoId) throws Exception {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "2");
        item.put("valorUnitario", "50.00");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("clienteId", clienteId);
        corpo.put("itens", List.of(item));
        corpo.put("percentualDesconto", "10");
        corpo.put("valorFrete", "15.00");
        corpo.put("prazoValidade", LocalDate.now().plusDays(7).toString());
        corpo.put("condicaoPagamento", "À vista");

        String resposta = mockMvc.perform(post("/api/orcamentos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String orcamentoId = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/orcamentos/" + orcamentoId + "/enviar")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/orcamentos/" + orcamentoId + "/aprovar")).andExpect(status().isOk());

        return orcamentoId;
    }

    private Map<String, Object> corpoPedido(String clienteId, List<String> produtoIds) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("clienteId", clienteId);
        corpo.put("itens", produtoIds.stream().map(produtoId -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("produtoId", produtoId);
            item.put("quantidade", "2");
            item.put("valorUnitario", "50.00");
            return item;
        }).toList());
        corpo.put("percentualDesconto", "10");
        corpo.put("valorFrete", "15.00");
        corpo.put("condicaoPagamento", "À vista");
        return corpo;
    }

    @Test
    void cadastraBuscaEEditaPedido() throws Exception {
        String clienteId = criarCliente("Maria Silva", "111.444.777-35", "maria@example.com");
        String categoriaId = criarCategoria("Alimentos");
        String unidadeId = criarUnidadeMedida("Unidade", "un");
        String produtoId = criarProduto("SKU-PED-001", "Arroz Branco", categoriaId, unidadeId);
        String transportadoraId = criarTransportadora("Rápido Entregas");

        Map<String, Object> corpo = corpoPedido(clienteId, List.of(produtoId));
        corpo.put("transportadoraId", transportadoraId);

        String resposta = mockMvc.perform(post("/api/pedidos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andExpect(jsonPath("$.valorTotal").value(105.00))
                .andExpect(jsonPath("$.clienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$.transportadoraNome").value("Rápido Entregas"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/pedidos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CRIADO"));

        Map<String, Object> corpoEditado = corpoPedido(clienteId, List.of(produtoId));
        corpoEditado.put("percentualDesconto", "0");

        mockMvc.perform(put("/api/pedidos/" + id).contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoEditado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorDesconto").value(0.00))
                .andExpect(jsonPath("$.valorTotal").value(115.00))
                .andExpect(jsonPath("$.transportadoraId").doesNotExist());
    }

    @Test
    void criaPedidoAPartirDeOrcamentoAprovado() throws Exception {
        String clienteId = criarCliente("Joana Souza", "987.654.321-00", "joana@example.com");
        String categoriaId = criarCategoria("Bebidas");
        String unidadeId = criarUnidadeMedida("Litro", "L");
        String produtoId = criarProduto("SKU-PED-002", "Suco", categoriaId, unidadeId);
        String orcamentoId = criarOrcamentoAprovado(clienteId, produtoId);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("orcamentoId", orcamentoId);

        mockMvc.perform(post("/api/pedidos/a-partir-de-orcamento").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orcamentoOrigemId").value(orcamentoId))
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andExpect(jsonPath("$.condicaoPagamento").value("À vista"));
    }

    @Test
    void rejeitaPedidoAPartirDeOrcamentoNaoAprovado() throws Exception {
        String clienteId = criarCliente("Cliente Rascunho", "123.456.789-09", "rascunho@example.com");
        String categoriaId = criarCategoria("Categoria Rascunho");
        String unidadeId = criarUnidadeMedida("Unidade Rascunho", "un");
        String produtoId = criarProduto("SKU-PED-003", "Produto Rascunho", categoriaId, unidadeId);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "1");
        item.put("valorUnitario", "10.00");

        Map<String, Object> corpoOrcamento = new LinkedHashMap<>();
        corpoOrcamento.put("clienteId", clienteId);
        corpoOrcamento.put("itens", List.of(item));
        corpoOrcamento.put("percentualDesconto", "0");
        corpoOrcamento.put("valorFrete", "0");
        corpoOrcamento.put("prazoValidade", LocalDate.now().plusDays(7).toString());
        corpoOrcamento.put("condicaoPagamento", "À vista");

        String resposta = mockMvc.perform(post("/api/orcamentos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoOrcamento)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String orcamentoId = objectMapper.readTree(resposta).get("id").asText();

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("orcamentoId", orcamentoId);

        mockMvc.perform(post("/api/pedidos/a-partir-de-orcamento").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void fluxoCompletoDeStatusAteEntregue() throws Exception {
        String clienteId = criarCliente("Cliente Fluxo", "445.566.778-40", "fluxo@example.com");
        String categoriaId = criarCategoria("Categoria Fluxo");
        String unidadeId = criarUnidadeMedida("Unidade Fluxo", "un");
        String produtoId = criarProduto("SKU-PED-004", "Produto Fluxo", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoPedido(clienteId, List.of(produtoId)));
        String resposta = mockMvc.perform(post("/api/pedidos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/pedidos/" + id + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
        mockMvc.perform(patch("/api/pedidos/" + id + "/iniciar-separacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_SEPARACAO"));
        mockMvc.perform(patch("/api/pedidos/" + id + "/marcar-pronto-para-entrega"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO_PARA_ENTREGA"));
        mockMvc.perform(patch("/api/pedidos/" + id + "/entregar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));

        mockMvc.perform(patch("/api/pedidos/" + id + "/cancelar"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelarFuncionaEmPedidoCriado() throws Exception {
        String clienteId = criarCliente("Cliente Cancelamento", "529.982.247-25", "cancelapedido@example.com");
        String categoriaId = criarCategoria("Categoria Cancelamento Pedido");
        String unidadeId = criarUnidadeMedida("Unidade Cancelamento Pedido", "un");
        String produtoId = criarProduto("SKU-PED-005", "Produto Cancelamento", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoPedido(clienteId, List.of(produtoId)));
        String resposta = mockMvc.perform(post("/api/pedidos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/pedidos/" + id + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void rejeitaClienteInexistente() throws Exception {
        Map<String, Object> corpo = corpoPedido(UUID.randomUUID().toString(), List.of());
        corpo.put("itens", List.of(Map.of("produtoId", UUID.randomUUID().toString(),
                "quantidade", "1", "valorUnitario", "10.00")));

        mockMvc.perform(post("/api/pedidos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaComFiltroDeStatus() throws Exception {
        String clienteId = criarCliente("Cliente Listagem Pedido", "998.877.665-00", "listagempedido@example.com");
        String categoriaId = criarCategoria("Categoria Listagem Pedido");
        String unidadeId = criarUnidadeMedida("Unidade Listagem Pedido", "un");
        String produtoId = criarProduto("SKU-PED-006", "Produto Listagem", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoPedido(clienteId, List.of(produtoId)));
        mockMvc.perform(post("/api/pedidos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/pedidos").param("clienteId", clienteId).param("status", "CRIADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteNome").value("Cliente Listagem Pedido"));
    }
}
