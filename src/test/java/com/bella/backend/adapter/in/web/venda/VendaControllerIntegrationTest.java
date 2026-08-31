package com.bella.backend.adapter.in.web.venda;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VendaControllerIntegrationTest extends IntegrationTestBase {

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

    private String criarPedidoEntregue(String clienteId, String produtoId) throws Exception {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "2");
        item.put("valorUnitario", "50.00");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("clienteId", clienteId);
        corpo.put("itens", List.of(item));
        corpo.put("percentualDesconto", "10");
        corpo.put("valorFrete", "15.00");
        corpo.put("condicaoPagamento", "À vista");

        String resposta = mockMvc.perform(post("/api/pedidos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String pedidoId = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/confirmar")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/iniciar-separacao")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/marcar-pronto-para-entrega")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/entregar")).andExpect(status().isOk());

        return pedidoId;
    }

    @Test
    void concluiVendaAPartirDePedidoEntregueEBusca() throws Exception {
        String clienteId = criarCliente("Maria Silva", "111.444.777-35", "maria@example.com");
        String categoriaId = criarCategoria("Alimentos");
        String unidadeId = criarUnidadeMedida("Unidade", "un");
        String produtoId = criarProduto("SKU-VDA-001", "Arroz Branco", categoriaId, unidadeId);
        String pedidoId = criarPedidoEntregue(clienteId, produtoId);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pedidoId", pedidoId);
        corpo.put("formaPagamento", "PIX");

        String resposta = mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.pedidoId").value(pedidoId))
                .andExpect(jsonPath("$.clienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$.formaPagamento").value("PIX"))
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andExpect(jsonPath("$.valorTotal").value(105.00))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/vendas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void rejeitaConcluirVendaDePedidoNaoEntregue() throws Exception {
        String clienteId = criarCliente("Cliente Nao Entregue", "987.654.321-00", "naoentregue@example.com");
        String categoriaId = criarCategoria("Categoria Nao Entregue");
        String unidadeId = criarUnidadeMedida("Unidade Nao Entregue", "un");
        String produtoId = criarProduto("SKU-VDA-002", "Produto Nao Entregue", categoriaId, unidadeId);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("produtoId", produtoId);
        item.put("quantidade", "1");
        item.put("valorUnitario", "10.00");

        Map<String, Object> corpoPedido = new LinkedHashMap<>();
        corpoPedido.put("clienteId", clienteId);
        corpoPedido.put("itens", List.of(item));
        corpoPedido.put("percentualDesconto", "0");
        corpoPedido.put("valorFrete", "0");
        corpoPedido.put("condicaoPagamento", "À vista");

        String resposta = mockMvc.perform(post("/api/pedidos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoPedido)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String pedidoId = objectMapper.readTree(resposta).get("id").asText();

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pedidoId", pedidoId);
        corpo.put("formaPagamento", "DINHEIRO");

        mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaConcluirVendaDuasVezesParaMesmoPedido() throws Exception {
        String clienteId = criarCliente("Cliente Duplicado", "123.456.789-09", "duplicado@example.com");
        String categoriaId = criarCategoria("Categoria Duplicado");
        String unidadeId = criarUnidadeMedida("Unidade Duplicado", "un");
        String produtoId = criarProduto("SKU-VDA-003", "Produto Duplicado", categoriaId, unidadeId);
        String pedidoId = criarPedidoEntregue(clienteId, produtoId);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pedidoId", pedidoId);
        corpo.put("formaPagamento", "CARTAO_CREDITO");

        mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelarVenda() throws Exception {
        String clienteId = criarCliente("Cliente Cancela Venda", "529.982.247-25", "cancelavenda@example.com");
        String categoriaId = criarCategoria("Categoria Cancela Venda");
        String unidadeId = criarUnidadeMedida("Unidade Cancela Venda", "un");
        String produtoId = criarProduto("SKU-VDA-004", "Produto Cancela Venda", categoriaId, unidadeId);
        String pedidoId = criarPedidoEntregue(clienteId, produtoId);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pedidoId", pedidoId);
        corpo.put("formaPagamento", "BOLETO");

        String resposta = mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/vendas/" + id + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mockMvc.perform(patch("/api/vendas/" + id + "/cancelar"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaPedidoInexistente() throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pedidoId", UUID.randomUUID().toString());
        corpo.put("formaPagamento", "PIX");

        mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaHistoricoComFiltroDeStatus() throws Exception {
        String clienteId = criarCliente("Cliente Historico", "998.877.665-00", "historico@example.com");
        String categoriaId = criarCategoria("Categoria Historico");
        String unidadeId = criarUnidadeMedida("Unidade Historico", "un");
        String produtoId = criarProduto("SKU-VDA-005", "Produto Historico", categoriaId, unidadeId);
        String pedidoId = criarPedidoEntregue(clienteId, produtoId);

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("pedidoId", pedidoId);
        corpo.put("formaPagamento", "TRANSFERENCIA");

        mockMvc.perform(post("/api/vendas").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/vendas").param("clienteId", clienteId).param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteNome").value("Cliente Historico"));
    }
}
