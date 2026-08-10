package com.bella.backend.adapter.in.web.produto;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProdutoControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String criarCategoria(String nome) throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", nome));
        String resposta = mockMvc.perform(post("/api/categorias").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarUnidadeMedida(String nome, String sigla) throws Exception {
        Map<String, Object> corpoMap = new LinkedHashMap<>();
        corpoMap.put("nome", nome);
        corpoMap.put("sigla", sigla);
        String corpo = objectMapper.writeValueAsString(corpoMap);
        String resposta = mockMvc.perform(post("/api/unidades-medida").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private Map<String, Object> corpoProduto(String codigoInterno, String codigoBarras, String nome,
                                              String categoriaId, String unidadeMedidaId) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigoInterno", codigoInterno);
        corpo.put("codigoBarras", codigoBarras);
        corpo.put("nome", nome);
        corpo.put("categoriaId", categoriaId);
        corpo.put("unidadeMedidaId", unidadeMedidaId);
        corpo.put("precoCusto", "10.00");
        corpo.put("precoVenda", "15.00");
        corpo.put("estoqueMinimo", "5");
        return corpo;
    }

    @Test
    void cadastraBuscaEditaEInativaProduto() throws Exception {
        String categoriaId = criarCategoria("Alimentos");
        String unidadeMedidaId = criarUnidadeMedida("Quilograma", "kg");

        String corpo = objectMapper.writeValueAsString(
                corpoProduto("SKU-001", "7891234567890", "Arroz Branco", categoriaId, unidadeMedidaId));

        String resposta = mockMvc.perform(post("/api/produtos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoInterno").value("SKU-001"))
                .andExpect(jsonPath("$.categoria.nome").value("Alimentos"))
                .andExpect(jsonPath("$.unidadeMedida.sigla").value("kg"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Arroz Branco"));

        Map<String, Object> corpoEdicao = new LinkedHashMap<>();
        corpoEdicao.put("codigoBarras", "7891234567890");
        corpoEdicao.put("nome", "Arroz Integral");
        corpoEdicao.put("categoriaId", categoriaId);
        corpoEdicao.put("unidadeMedidaId", unidadeMedidaId);
        corpoEdicao.put("precoCusto", "12.00");
        corpoEdicao.put("precoVenda", "18.00");
        corpoEdicao.put("estoqueMinimo", "3");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/produtos/" + id)
                        .contentType("application/json").content(objectMapper.writeValueAsString(corpoEdicao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Arroz Integral"))
                .andExpect(jsonPath("$.codigoInterno").value("SKU-001"));

        mockMvc.perform(patch("/api/produtos/" + id + "/status")
                        .contentType("application/json").content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void rejeitaCodigoInternoDuplicado() throws Exception {
        String categoriaId = criarCategoria("Alimentos");
        String unidadeMedidaId = criarUnidadeMedida("Quilograma", "kg");

        String corpo1 = objectMapper.writeValueAsString(
                corpoProduto("SKU-DUP", null, "Produto Um", categoriaId, unidadeMedidaId));
        mockMvc.perform(post("/api/produtos").contentType("application/json").content(corpo1))
                .andExpect(status().isCreated());

        String corpo2 = objectMapper.writeValueAsString(
                corpoProduto("SKU-DUP", null, "Produto Dois", categoriaId, unidadeMedidaId));
        mockMvc.perform(post("/api/produtos").contentType("application/json").content(corpo2))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaCodigoBarrasDuplicado() throws Exception {
        String categoriaId = criarCategoria("Alimentos");
        String unidadeMedidaId = criarUnidadeMedida("Quilograma", "kg");

        String corpo1 = objectMapper.writeValueAsString(
                corpoProduto("SKU-100", "1112223334445", "Produto Um", categoriaId, unidadeMedidaId));
        mockMvc.perform(post("/api/produtos").contentType("application/json").content(corpo1))
                .andExpect(status().isCreated());

        String corpo2 = objectMapper.writeValueAsString(
                corpoProduto("SKU-101", "1112223334445", "Produto Dois", categoriaId, unidadeMedidaId));
        mockMvc.perform(post("/api/produtos").contentType("application/json").content(corpo2))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaPrecoNegativo() throws Exception {
        String categoriaId = criarCategoria("Alimentos");
        String unidadeMedidaId = criarUnidadeMedida("Quilograma", "kg");

        Map<String, Object> corpo = corpoProduto("SKU-200", null, "Produto Negativo", categoriaId, unidadeMedidaId);
        corpo.put("precoCusto", "-1.00");

        mockMvc.perform(post("/api/produtos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaCategoriaInexistente() throws Exception {
        String unidadeMedidaId = criarUnidadeMedida("Quilograma", "kg");

        Map<String, Object> corpo = corpoProduto("SKU-300", null, "Produto Sem Categoria",
                UUID.randomUUID().toString(), unidadeMedidaId);

        mockMvc.perform(post("/api/produtos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaProdutosFiltrandoPorBuscaEStatus() throws Exception {
        String categoriaId = criarCategoria("Bebidas");
        String unidadeMedidaId = criarUnidadeMedida("Litro", "L");

        String corpo = objectMapper.writeValueAsString(
                corpoProduto("SKU-400", null, "Suco de Laranja", categoriaId, unidadeMedidaId));
        mockMvc.perform(post("/api/produtos").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/produtos").param("busca", "Suco").param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Suco de Laranja"));
    }
}
