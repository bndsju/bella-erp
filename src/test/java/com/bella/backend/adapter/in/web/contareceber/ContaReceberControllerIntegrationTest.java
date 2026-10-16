package com.bella.backend.adapter.in.web.contareceber;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContaReceberControllerIntegrationTest extends IntegrationTestBase {

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

    private Map<String, Object> corpoUnico(String valor, LocalDate vencimento) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("descricao", "Mensalidade");
        corpo.put("valorTotal", valor);
        corpo.put("vencimento", vencimento.toString());
        return corpo;
    }

    private JsonNode criar(Map<String, Object> corpo) throws Exception {
        String resposta = mockMvc.perform(post("/api/contas-receber").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta);
    }

    private String corpoRecebimento(String parcelaId, String valor) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("parcelaId", parcelaId);
        corpo.put("valorRecebido", valor);
        return objectMapper.writeValueAsString(corpo);
    }

    @Test
    void criaContaUnicaSemCliente() throws Exception {
        mockMvc.perform(post("/api/contas-receber").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoUnico("250.00", LocalDate.now().plusDays(10)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.origem").value("MANUAL"))
                .andExpect(jsonPath("$.clienteId").doesNotExist())
                .andExpect(jsonPath("$.saldoPendente").value(250.00))
                .andExpect(jsonPath("$.parcelas.length()").value(1));
    }

    @Test
    void recebimentoParcialEDepoisTotalQuitaAConta() throws Exception {
        JsonNode conta = criar(corpoUnico("100.00", LocalDate.now().plusDays(10)));
        String contaId = conta.get("id").asText();
        String parcelaId = conta.get("parcelas").get(0).get("id").asText();

        mockMvc.perform(post("/api/contas-receber/" + contaId + "/recebimentos").contentType("application/json")
                        .content(corpoRecebimento(parcelaId, "40.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PARCIALMENTE_RECEBIDA"))
                .andExpect(jsonPath("$.saldoPendente").value(60.00));

        mockMvc.perform(post("/api/contas-receber/" + contaId + "/recebimentos").contentType("application/json")
                        .content(corpoRecebimento(parcelaId, "60.01")))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/contas-receber/" + contaId + "/recebimentos").contentType("application/json")
                        .content(corpoRecebimento(parcelaId, "60.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.parcelas[0].recebimentos.length()").value(2));
    }

    @Test
    void rejeitaSomaDeParcelasDiferenteDoValor() throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("descricao", "Venda parcelada");
        corpo.put("valorTotal", "300.00");
        corpo.put("parcelas", List.of(
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(30).toString()),
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(60).toString())));

        mockMvc.perform(post("/api/contas-receber").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void contaComRecebimentoNaoPodeSerCanceladaNemTerValorEditado() throws Exception {
        JsonNode conta = criar(corpoUnico("100.00", LocalDate.now().plusDays(10)));
        String contaId = conta.get("id").asText();
        String parcelaId = conta.get("parcelas").get(0).get("id").asText();

        mockMvc.perform(post("/api/contas-receber/" + contaId + "/recebimentos").contentType("application/json")
                        .content(corpoRecebimento(parcelaId, "10.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/contas-receber/" + contaId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Engano\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(put("/api/contas-receber/" + contaId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpoUnico("999.00", LocalDate.now().plusDays(10)))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch("/api/contas-receber/" + contaId + "/dados-basicos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("descricao", "Mensalidade - outubro"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Mensalidade - outubro"));
    }

    @Test
    void cancelaContaSemRecebimentosExigindoMotivo() throws Exception {
        String contaId = criar(corpoUnico("50.00", LocalDate.now().plusDays(10))).get("id").asText();

        mockMvc.perform(patch("/api/contas-receber/" + contaId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\" \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/contas-receber/" + contaId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Lançamento duplicado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void listaVencidasFiltrandoPorCliente() throws Exception {
        String clienteId = criarCliente("Maria Silva", "529.982.247-25", "maria.receber@example.com");
        Map<String, Object> corpo = corpoUnico("80.00", LocalDate.now().minusDays(5));
        corpo.put("clienteId", clienteId);
        String contaId = criar(corpo).get("id").asText();

        mockMvc.perform(get("/api/contas-receber").param("clienteId", clienteId).param("situacao", "VENCIDAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contaId))
                .andExpect(jsonPath("$[0].clienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$[0].vencida").value(true));
    }
}
