package com.bella.backend.adapter.in.web.cliente;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cadastraBuscaEditaEInativaClientePessoaFisica() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                clientePfValido("Maria Silva", "111.444.777-35", "maria@example.com"));

        String resposta = mockMvc.perform(post("/api/clientes").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoPessoa").value("PF"))
                .andExpect(jsonPath("$.nomeCompleto").value("Maria Silva"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(resposta).get("id").asText();

        mockMvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("11144477735"));

        String corpoEditado = objectMapper.writeValueAsString(
                clientePfValido("Maria S. Silva", "111.444.777-35", "maria2@example.com"));

        mockMvc.perform(put("/api/clientes/" + id).contentType("application/json").content(corpoEditado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Maria S. Silva"))
                .andExpect(jsonPath("$.cpf").value("11144477735"));

        mockMvc.perform(patch("/api/clientes/" + id + "/status")
                        .contentType("application/json").content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void cadastraClientePessoaJuridica() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                clientePjValido("Padaria Pão Quente Ltda", "11.222.333/0001-81", "contato@paoquente.com"));

        mockMvc.perform(post("/api/clientes").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoPessoa").value("PJ"))
                .andExpect(jsonPath("$.cnpj").value("11222333000181"));
    }

    @Test
    void rejeitaCadastroComCpfDuplicado() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                clientePfValido("Cliente Um", "123.456.789-09", "um@example.com"));
        mockMvc.perform(post("/api/clientes").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        String corpoDuplicado = objectMapper.writeValueAsString(
                clientePfValido("Cliente Dois", "123.456.789-09", "dois@example.com"));
        mockMvc.perform(post("/api/clientes").contentType("application/json").content(corpoDuplicado))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void rejeitaDataDeNascimentoFutura() throws Exception {
        Map<String, Object> corpo = clientePfValido("Cliente Futuro", "111.444.777-35", "futuro@example.com");
        corpo.put("dataNascimento", "2999-01-01");

        mockMvc.perform(post("/api/clientes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retorna404ParaClienteInexistente() throws Exception {
        mockMvc.perform(get("/api/clientes/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaClientesFiltrandoPorBuscaEStatus() throws Exception {
        String corpo = objectMapper.writeValueAsString(
                clientePfValido("Joana Souza", "987.654.321-00", "joana@example.com"));
        mockMvc.perform(post("/api/clientes").contentType("application/json").content(corpo))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/clientes").param("busca", "Joana").param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nomeCompleto", hasItem("Joana Souza")));
    }

    private Map<String, Object> clientePfValido(String nome, String cpf, String email) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tipoPessoa", "PF");
        corpo.put("nomeCompleto", nome);
        corpo.put("dataNascimento", "1990-05-10");
        corpo.put("cpf", cpf);
        corpo.put("celular", "11999998888");
        corpo.put("email", email);
        corpo.put("endereco", enderecoValido());
        return corpo;
    }

    private Map<String, Object> clientePjValido(String razaoSocial, String cnpj, String email) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tipoPessoa", "PJ");
        corpo.put("razaoSocial", razaoSocial);
        corpo.put("cnpj", cnpj);
        corpo.put("celular", "11999998888");
        corpo.put("email", email);
        corpo.put("endereco", enderecoValido());
        return corpo;
    }

    private Map<String, Object> enderecoValido() {
        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01310-100");
        endereco.put("logradouro", "Av. Paulista");
        endereco.put("numero", "1000");
        endereco.put("bairro", "Bela Vista");
        endereco.put("cidade", "São Paulo");
        endereco.put("uf", "SP");
        return endereco;
    }
}
