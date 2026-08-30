package com.bella.backend.adapter.in.web.unidademedida;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnidadeMedidaControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> corpo(String nome, String sigla) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("sigla", sigla);
        return corpo;
    }

    @Test
    void cadastraBuscaEditaEInativaUnidadeMedida() throws Exception {
        String corpo = objectMapper.writeValueAsString(corpo("Quilograma", "kg"));

        String resposta = mockMvc.perform(post("/api/unidades-medida").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sigla").value("kg"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/unidades-medida/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Quilograma"));

        String corpoEditado = objectMapper.writeValueAsString(corpo("Grama", "g"));
        mockMvc.perform(put("/api/unidades-medida/" + id).contentType("application/json").content(corpoEditado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sigla").value("g"));

        mockMvc.perform(patch("/api/unidades-medida/" + id + "/status")
                        .contentType("application/json").content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void rejeitaSiglaVazia() throws Exception {
        String corpo = objectMapper.writeValueAsString(corpo("Quilograma", ""));

        mockMvc.perform(post("/api/unidades-medida").contentType("application/json").content(corpo))
                .andExpect(status().isBadRequest());
    }
}
