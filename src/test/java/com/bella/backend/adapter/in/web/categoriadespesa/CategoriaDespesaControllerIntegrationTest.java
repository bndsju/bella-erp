package com.bella.backend.adapter.in.web.categoriadespesa;

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

class CategoriaDespesaControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cadastraBuscaEditaEInativaCategoriaDespesa() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", "Bebidas"));

        String resposta = mockMvc.perform(post("/api/categoriasDespesa-despesa-despesa").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Bebidas"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/categoriasDespesa-despesa-despesa/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebidas"));

        String corpoEditado = objectMapper.writeValueAsString(Map.of("nome", "Bebidas Geladas"));
        mockMvc.perform(put("/api/categoriasDespesa-despesa-despesa/" + id).contentType("application/json").content(corpoEditado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebidas Geladas"));

        mockMvc.perform(patch("/api/categoriasDespesa-despesa-despesa/" + id + "/status")
                        .contentType("application/json").content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void rejeitaNomeVazio() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", ""));

        mockMvc.perform(post("/api/categoriasDespesa-despesa-despesa").contentType("application/json").content(corpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaCategoriaDespesasFiltrandoPorStatus() throws Exception {
        String corpo = objectMapper.writeValueAsString(Map.of("nome", "Laticínios"));
        mockMvc.perform(post("/api/categoriasDespesa-despesa-despesa").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/categoriasDespesa-despesa-despesa").param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", hasItem("Laticínios")));
    }
}
