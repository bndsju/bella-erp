package com.bella.backend.adapter.in.web.orcamento;

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

class OrcamentoControllerIntegrationTest extends IntegrationTestBase {

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

    private Map<String, Object> corpoOrcamento(String clienteId, List<String> produtoIds) {
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
        corpo.put("prazoValidade", LocalDate.now().plusDays(7).toString());
        corpo.put("condicaoPagamento", "À vista");
        return corpo;
    }

    @Test
    void cadastraBuscaEEditaOrcamento() throws Exception {
        String clienteId = criarCliente("Maria Silva", "111.444.777-35", "maria@example.com");
        String categoriaId = criarCategoria("Alimentos");
        String unidadeId = criarUnidadeMedida("Unidade", "un");
        String produtoId = criarProduto("SKU-ORC-001", "Arroz Branco", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoOrcamento(clienteId, List.of(produtoId)));

        String resposta = mockMvc.perform(post("/api/orcamentos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.subtotal").value(100.00))
                .andExpect(jsonPath("$.valorDesconto").value(10.00))
                .andExpect(jsonPath("$.valorTotal").value(105.00))
                .andExpect(jsonPath("$.clienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$.itens[0].produtoNome").value("Arroz Branco"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/orcamentos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        Map<String, Object> corpoEditado = corpoOrcamento(clienteId, List.of(produtoId));
        corpoEditado.put("percentualDesconto", "0");

        mockMvc.perform(put("/api/orcamentos/" + id).contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoEditado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorDesconto").value(0.00))
                .andExpect(jsonPath("$.valorTotal").value(115.00));
    }

    @Test
    void fluxoEnviarEAprovar() throws Exception {
        String clienteId = criarCliente("Joana Souza", "987.654.321-00", "joana@example.com");
        String categoriaId = criarCategoria("Bebidas");
        String unidadeId = criarUnidadeMedida("Litro", "L");
        String produtoId = criarProduto("SKU-ORC-002", "Suco", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoOrcamento(clienteId, List.of(produtoId)));
        String resposta = mockMvc.perform(post("/api/orcamentos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/orcamentos/" + id + "/enviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));

        mockMvc.perform(patch("/api/orcamentos/" + id + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mockMvc.perform(patch("/api/orcamentos/" + id + "/cancelar"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelarFuncionaEmRascunho() throws Exception {
        String clienteId = criarCliente("Cliente Cancelamento", "123.456.789-09", "cancel@example.com");
        String categoriaId = criarCategoria("Categoria Cancelamento");
        String unidadeId = criarUnidadeMedida("Peça", "pç");
        String produtoId = criarProduto("SKU-ORC-003", "Produto Cancelamento", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoOrcamento(clienteId, List.of(produtoId)));
        String resposta = mockMvc.perform(post("/api/orcamentos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(patch("/api/orcamentos/" + id + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void rejeitaClienteInexistente() throws Exception {
        Map<String, Object> corpo = corpoOrcamento(UUID.randomUUID().toString(), List.of());
        corpo.put("itens", List.of(Map.of("produtoId", UUID.randomUUID().toString(),
                "quantidade", "1", "valorUnitario", "10.00")));

        mockMvc.perform(post("/api/orcamentos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaPercentualDescontoAcimaDeCem() throws Exception {
        String clienteId = criarCliente("Cliente Desconto", "112.223.334-57", "desconto@example.com");
        String categoriaId = criarCategoria("Categoria Desconto");
        String unidadeId = criarUnidadeMedida("Unidade Desconto", "un");
        String produtoId = criarProduto("SKU-ORC-004", "Produto Desconto", categoriaId, unidadeId);

        Map<String, Object> corpo = corpoOrcamento(clienteId, List.of(produtoId));
        corpo.put("percentualDesconto", "150");

        mockMvc.perform(post("/api/orcamentos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaPrazoValidadeNoPassado() throws Exception {
        String clienteId = criarCliente("Cliente Prazo", "529.982.247-25", "prazo@example.com");
        String categoriaId = criarCategoria("Categoria Prazo");
        String unidadeId = criarUnidadeMedida("Unidade Prazo", "un");
        String produtoId = criarProduto("SKU-ORC-005", "Produto Prazo", categoriaId, unidadeId);

        Map<String, Object> corpo = corpoOrcamento(clienteId, List.of(produtoId));
        corpo.put("prazoValidade", LocalDate.now().minusDays(1).toString());

        mockMvc.perform(post("/api/orcamentos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaComFiltroDeStatus() throws Exception {
        String clienteId = criarCliente("Cliente Listagem", "998.877.665-00", "listagem@example.com");
        String categoriaId = criarCategoria("Categoria Listagem");
        String unidadeId = criarUnidadeMedida("Unidade Listagem", "un");
        String produtoId = criarProduto("SKU-ORC-006", "Produto Listagem", categoriaId, unidadeId);

        String corpo = objectMapper.writeValueAsString(corpoOrcamento(clienteId, List.of(produtoId)));
        mockMvc.perform(post("/api/orcamentos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orcamentos").param("clienteId", clienteId).param("status", "RASCUNHO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteNome").value("Cliente Listagem"));
    }
}
