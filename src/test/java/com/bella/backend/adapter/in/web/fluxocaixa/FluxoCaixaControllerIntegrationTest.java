package com.bella.backend.adapter.in.web.fluxocaixa;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FluxoCaixaControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String criarCategoria(String nome) throws Exception {
        String resposta = mockMvc.perform(post("/api/categorias-fluxo-caixa").contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("nome", nome))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private Map<String, Object> corpo(String tipo, String categoriaId, String valor, String situacao,
                                       LocalDate dataPrevista, LocalDate dataEfetiva) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("tipo", tipo);
        corpo.put("descricao", "Lançamento " + tipo);
        corpo.put("categoriaId", categoriaId);
        corpo.put("valor", valor);
        corpo.put("situacao", situacao);
        corpo.put("dataPrevista", dataPrevista.toString());
        if (dataEfetiva != null) {
            corpo.put("dataEfetiva", dataEfetiva.toString());
        }
        return corpo;
    }

    private JsonNode lancar(Map<String, Object> corpo) throws Exception {
        String resposta = mockMvc.perform(post("/api/fluxo-caixa/movimentacoes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta);
    }

    @Test
    void lancaEntradaPrevistaSemAlterarSaldoRealizado() throws Exception {
        String categoriaId = criarCategoria("Receitas A");
        LocalDate vencimento = LocalDate.now().plusDays(3);

        mockMvc.perform(post("/api/fluxo-caixa/movimentacoes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpo("ENTRADA", categoriaId, "500.00", "PREVISTO", vencimento, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origem").value("LANCAMENTO_MANUAL"))
                .andExpect(jsonPath("$.situacao").value("PREVISTO"))
                .andExpect(jsonPath("$.categoriaNome").value("Receitas A"));

        mockMvc.perform(get("/api/fluxo-caixa/resumo")
                        .param("inicio", vencimento.toString()).param("fim", vencimento.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entradasPrevistas").value(500.00))
                .andExpect(jsonPath("$.saldoRealizado").value(0))
                .andExpect(jsonPath("$.saldoProjetado").value(500.00));
    }

    @Test
    void realizarOMesmoRegistroNaoDobraOSaldo() throws Exception {
        String categoriaId = criarCategoria("Receitas B");
        LocalDate dia = LocalDate.now().minusDays(400);
        String id = lancar(corpo("ENTRADA", categoriaId, "500.00", "PREVISTO", dia, null)).get("id").asText();

        mockMvc.perform(patch("/api/fluxo-caixa/movimentacoes/" + id + "/realizar").contentType("application/json")
                        .content("{\"dataEfetiva\":\"" + dia + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("REALIZADO"))
                .andExpect(jsonPath("$.dataEfetiva").value(dia.toString()));

        mockMvc.perform(get("/api/fluxo-caixa/resumo")
                        .param("inicio", dia.toString()).param("fim", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entradasPrevistas").value(0))
                .andExpect(jsonPath("$.entradasRealizadas").value(500.00))
                .andExpect(jsonPath("$.saldoRealizado").value(500.00))
                .andExpect(jsonPath("$.saldoProjetado").value(500.00));

        mockMvc.perform(patch("/api/fluxo-caixa/movimentacoes/" + id + "/realizar"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void saidaRealizadaDiminuiOSaldoERealizadoExigeDataEfetiva() throws Exception {
        String categoriaId = criarCategoria("Despesas A");
        LocalDate dia = LocalDate.now().minusDays(800);

        mockMvc.perform(post("/api/fluxo-caixa/movimentacoes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpo("SAIDA", categoriaId, "120.00", "REALIZADO", dia, null))))
                .andExpect(status().isUnprocessableEntity());

        lancar(corpo("SAIDA", categoriaId, "120.00", "REALIZADO", dia, dia));

        mockMvc.perform(get("/api/fluxo-caixa/resumo")
                        .param("inicio", dia.toString()).param("fim", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saidasRealizadas").value(120.00))
                .andExpect(jsonPath("$.saldoRealizado").value(-120.00));
    }

    @Test
    void rejeitaValorInvalidoELancamentoRealizadoNaoPodeSerEditadoMasPodeSerEstornado() throws Exception {
        String categoriaId = criarCategoria("Despesas B");
        LocalDate dia = LocalDate.now().minusDays(1200);

        mockMvc.perform(post("/api/fluxo-caixa/movimentacoes").contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpo("SAIDA", categoriaId, "0", "PREVISTO", dia, null))))
                .andExpect(status().isBadRequest());

        String id = lancar(corpo("SAIDA", categoriaId, "80.00", "REALIZADO", dia, dia)).get("id").asText();

        mockMvc.perform(put("/api/fluxo-caixa/movimentacoes/" + id).contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                corpo("SAIDA", categoriaId, "90.00", "PREVISTO", dia, null))))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(patch("/api/fluxo-caixa/movimentacoes/" + id + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/fluxo-caixa/movimentacoes/" + id + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Estorno de lançamento incorreto\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelada").value(true));

        mockMvc.perform(get("/api/fluxo-caixa/resumo")
                        .param("inicio", dia.toString()).param("fim", dia.toString()))
                .andExpect(jsonPath("$.saidasRealizadas").value(0));
        mockMvc.perform(get("/api/fluxo-caixa/movimentacoes").param("incluirCanceladas", "true")
                        .param("dataInicio", dia.toString()).param("dataFim", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void filtraPorTipoSituacaoEOrigemEResumePorCategoria() throws Exception {
        String categoriaId = criarCategoria("Receitas C");
        LocalDate dia = LocalDate.now().minusDays(1600);
        lancar(corpo("ENTRADA", categoriaId, "300.00", "REALIZADO", dia, dia));
        lancar(corpo("SAIDA", categoriaId, "100.00", "PREVISTO", dia, null));

        mockMvc.perform(get("/api/fluxo-caixa/movimentacoes").param("tipo", "ENTRADA")
                        .param("situacao", "REALIZADO").param("origem", "LANCAMENTO_MANUAL")
                        .param("categoriaId", categoriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].valor").value(300.00));

        mockMvc.perform(get("/api/fluxo-caixa/resumo-por-categoria")
                        .param("inicio", dia.toString()).param("fim", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaNome").value("Receitas C"))
                .andExpect(jsonPath("$[0].resumo.saldoRealizado").value(300.00))
                .andExpect(jsonPath("$[0].resumo.saldoProjetado").value(200.00));
    }

    @Test
    void consultaSaldoAcumuladoAteUmaData() throws Exception {
        String categoriaId = criarCategoria("Receitas D");
        LocalDate dia = LocalDate.now().minusDays(2000);
        lancar(corpo("ENTRADA", categoriaId, "1000.00", "REALIZADO", dia, dia));

        mockMvc.perform(get("/api/fluxo-caixa/saldo-acumulado").param("ate", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoRealizado").value(1000.00));
    }
}
