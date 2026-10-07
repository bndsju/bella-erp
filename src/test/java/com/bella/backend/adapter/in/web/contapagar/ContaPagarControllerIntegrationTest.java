package com.bella.backend.adapter.in.web.contapagar;

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

class ContaPagarControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String criarCategoriaDespesa(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias-despesa").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("nome", nome))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String criarFornecedor(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/fornecedores").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("nome", nome))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private Map<String, Object> corpoUnico(String categoriaId, String valor, LocalDate vencimento) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("descricao", "Energia elétrica");
        corpo.put("categoriaDespesaId", categoriaId);
        corpo.put("valorTotal", valor);
        corpo.put("vencimento", vencimento.toString());
        return corpo;
    }

    private JsonNode criar(Map<String, Object> corpo) throws Exception {
        String resposta = mockMvc.perform(post("/api/contas-pagar").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta);
    }

    private String corpoPagamento(String parcelaId, String valor) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("parcelaId", parcelaId);
        corpo.put("valorPago", valor);
        return objectMapper.writeValueAsString(corpo);
    }

    @Test
    void criaContaUnicaSemFornecedor() throws Exception {
        String categoriaId = criarCategoriaDespesa("Utilidades");

        mockMvc.perform(post("/api/contas-pagar").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpoUnico(categoriaId, "250.00", LocalDate.now().plusDays(10)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.origem").value("MANUAL"))
                .andExpect(jsonPath("$.fornecedorId").doesNotExist())
                .andExpect(jsonPath("$.categoriaDespesaNome").value("Utilidades"))
                .andExpect(jsonPath("$.saldoPendente").value(250.00))
                .andExpect(jsonPath("$.parcelas.length()").value(1));
    }

    @Test
    void pagamentoParcialEDepoisTotalQuitaAConta() throws Exception {
        String categoriaId = criarCategoriaDespesa("Aluguel");
        JsonNode conta = criar(corpoUnico(categoriaId, "100.00", LocalDate.now().plusDays(10)));
        String contaId = conta.get("id").asText();
        String parcelaId = conta.get("parcelas").get(0).get("id").asText();

        mockMvc.perform(post("/api/contas-pagar/" + contaId + "/pagamentos").contentType("application/json")
                        .content(corpoPagamento(parcelaId, "40.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PARCIALMENTE_PAGA"))
                .andExpect(jsonPath("$.saldoPendente").value(60.00));

        mockMvc.perform(post("/api/contas-pagar/" + contaId + "/pagamentos").contentType("application/json")
                        .content(corpoPagamento(parcelaId, "60.01")))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post("/api/contas-pagar/" + contaId + "/pagamentos").contentType("application/json")
                        .content(corpoPagamento(parcelaId, "60.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAGA"))
                .andExpect(jsonPath("$.parcelas[0].pagamentos.length()").value(2));
    }

    @Test
    void rejeitaSomaDeParcelasDiferenteDoValor() throws Exception {
        String categoriaId = criarCategoriaDespesa("Equipamentos");
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("descricao", "Máquina");
        corpo.put("categoriaDespesaId", categoriaId);
        corpo.put("valorTotal", "300.00");
        corpo.put("parcelas", List.of(
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(30).toString()),
                Map.of("valor", "100.00", "vencimento", LocalDate.now().plusDays(60).toString())));

        mockMvc.perform(post("/api/contas-pagar").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void contaComPagamentoNaoPodeSerCanceladaNemTerValorEditado() throws Exception {
        String categoriaId = criarCategoriaDespesa("Internet");
        JsonNode conta = criar(corpoUnico(categoriaId, "100.00", LocalDate.now().plusDays(10)));
        String contaId = conta.get("id").asText();
        String parcelaId = conta.get("parcelas").get(0).get("id").asText();

        mockMvc.perform(post("/api/contas-pagar/" + contaId + "/pagamentos").contentType("application/json")
                        .content(corpoPagamento(parcelaId, "10.00")))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/contas-pagar/" + contaId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Engano\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(put("/api/contas-pagar/" + contaId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpoUnico(categoriaId, "999.00", LocalDate.now().plusDays(10)))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch("/api/contas-pagar/" + contaId + "/dados-basicos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("descricao", "Internet - outubro",
                                "categoriaDespesaId", categoriaId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Internet - outubro"));
    }

    @Test
    void cancelaContaSemPagamentosExigindoMotivo() throws Exception {
        String categoriaId = criarCategoriaDespesa("Outras");
        String contaId = criar(corpoUnico(categoriaId, "50.00", LocalDate.now().plusDays(10))).get("id").asText();

        mockMvc.perform(patch("/api/contas-pagar/" + contaId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\" \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/contas-pagar/" + contaId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Lançamento duplicado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void listaVencidasEFiltraPorFornecedorECategoria() throws Exception {
        String categoriaId = criarCategoriaDespesa("Serviços");
        String fornecedorId = criarFornecedor("Fornecedor de Serviços");
        Map<String, Object> corpo = corpoUnico(categoriaId, "80.00", LocalDate.now().minusDays(5));
        corpo.put("fornecedorId", fornecedorId);
        String contaId = criar(corpo).get("id").asText();

        mockMvc.perform(get("/api/contas-pagar").param("fornecedorId", fornecedorId)
                        .param("categoriaDespesaId", categoriaId).param("situacao", "VENCIDAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(contaId))
                .andExpect(jsonPath("$[0].vencida").value(true));
    }
}
