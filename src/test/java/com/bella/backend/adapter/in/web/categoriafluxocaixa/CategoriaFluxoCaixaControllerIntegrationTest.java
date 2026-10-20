package com.bella.backend.adapter.in.web.categoriafluxocaixa;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaFluxoCaixaControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cadastraBuscaEditaEInativaCategoriaFluxoCaixa() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", "Bebidas"));

        String resposta = mockMvc.perform(post("/api/categorias-fluxo-caixa").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Bebidas"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/categorias-fluxo-caixa/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebidas"));

        String corpoEditado = objectMapper.writeValueAsString(Map.of("nome", "Bebidas Geladas"));
        mockMvc.perform(put("/api/categorias-fluxo-caixa/" + id).contentType("application/json").content(corpoEditado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebidas Geladas"));

        mockMvc.perform(patch("/api/categorias-fluxo-caixa/" + id + "/status")
                        .contentType("application/json").content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void rejeitaNomeVazio() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", ""));

        mockMvc.perform(post("/api/categorias-fluxo-caixa").contentType("application/json").content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaCategoriaFluxoCaixasFiltrandoPorStatus() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", "Laticínios"));
        mockMvc.perform(post("/api/categorias-fluxo-caixa").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categorias-fluxo-caixa").param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Laticínios")));
    }
}
