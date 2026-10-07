package com.bella.backend.adapter.in.web.compra;

import com.bella.backend.IntegrationTestBase;
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

class CompraControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String criarFornecedor(String nome) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("nome", nome);
        corpo.put("telefone", "1133334444");
        String resposta = mockMvc.perform(post("/api/fornecedores").contentType("application/json")
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

    private String criarProduto(String codigoInterno, String nome, String categoriaId, String unidadeId)
            throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigoInterno", codigoInterno);
        corpo.put("nome", nome);
        corpo.put("categoriaId", categoriaId);
        corpo.put("unidadeMedidaId", unidadeId);
        corpo.put("precoCusto", "30.00");
        corpo.put("precoVenda", "50.00");
        corpo.put("estoqueMinimo", "0");
        String resposta = mockMvc.perform(post("/api/produtos").contentType("application/json")
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    private String corpoCompra(String fornecedorId, List<String> produtoIds) throws Exception {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("fornecedorId", fornecedorId);
        corpo.put("previsaoRecebimento", LocalDate.now().plusDays(5).toString());
        corpo.put("itens", produtoIds.stream().map(produtoId -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("produtoId", produtoId);
            item.put("quantidade", "10");
            item.put("custoUnitario", "5.00");
            item.put("desconto", "5.00");
            return item;
        }).toList());
        corpo.put("descontoGeral", "0");
        corpo.put("frete", "10.00");
        corpo.put("outrasDespesas", "2.00");
        corpo.put("condicaoPagamento", "30 dias");
        return objectMapper.writeValueAsString(corpo);
    }

    private String criarCompra(String fornecedorId, List<String> produtoIds) throws Exception {
        String resposta = mockMvc.perform(post("/api/compras").contentType("application/json")
                        .content(corpoCompra(fornecedorId, produtoIds)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asText();
    }

    @Test
    void criaCompraCalculandoTotais() throws Exception {
        String fornecedorId = criarFornecedor("Fornecedor Totais");
        String categoriaId = criarCategoria("Categoria Compra 1");
        String unidadeId = criarUnidadeMedida("Unidade Compra 1", "un");
        String produtoId = criarProduto("SKU-CMP-001", "Produto Compra 1", categoriaId, unidadeId);

        mockMvc.perform(post("/api/compras").contentType("application/json")
                        .content(corpoCompra(fornecedorId, List.of(produtoId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.fornecedorNome").value("Fornecedor Totais"))
                .andExpect(jsonPath("$.valorProdutos").value(50.00))
                .andExpect(jsonPath("$.descontoTotal").value(5.00))
                .andExpect(jsonPath("$.valorTotal").value(57.00));
    }

    @Test
    void recebimentoGeraEntradaNoEstoqueUmaUnicaVez() throws Exception {
        String fornecedorId = criarFornecedor("Fornecedor Recebimento");
        String categoriaId = criarCategoria("Categoria Compra 2");
        String unidadeId = criarUnidadeMedida("Unidade Compra 2", "un");
        String produtoA = criarProduto("SKU-CMP-002", "Produto Compra A", categoriaId, unidadeId);
        String produtoB = criarProduto("SKU-CMP-003", "Produto Compra B", categoriaId, unidadeId);
        String compraId = criarCompra(fornecedorId, List.of(produtoA, produtoB));

        mockMvc.perform(patch("/api/compras/" + compraId + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        mockMvc.perform(get("/api/compras/pendentes-recebimento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + compraId + "')]").exists());

        mockMvc.perform(patch("/api/compras/" + compraId + "/receber"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataRecebimento").exists());

        mockMvc.perform(get("/api/estoque/produtos/" + produtoA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(10));
        mockMvc.perform(get("/api/estoque/produtos/" + produtoB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(10));

        mockMvc.perform(patch("/api/compras/" + compraId + "/receber"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/estoque/produtos/" + produtoA))
                .andExpect(jsonPath("$.estoqueFisico").value(10));

        mockMvc.perform(get("/api/estoque/movimentacoes").param("produtoId", produtoA).param("tipo", "ENTRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origem").value("COMPRA"))
                .andExpect(jsonPath("$[0].origemOperacaoId").value(compraId));
    }

    @Test
    void compraRecebidaNaoPodeSerEditadaNemCancelada() throws Exception {
        String fornecedorId = criarFornecedor("Fornecedor Imutavel");
        String categoriaId = criarCategoria("Categoria Compra 3");
        String unidadeId = criarUnidadeMedida("Unidade Compra 3", "un");
        String produtoId = criarProduto("SKU-CMP-004", "Produto Compra 3", categoriaId, unidadeId);
        String compraId = criarCompra(fornecedorId, List.of(produtoId));

        mockMvc.perform(patch("/api/compras/" + compraId + "/confirmar")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/compras/" + compraId + "/receber")).andExpect(status().isOk());

        mockMvc.perform(put("/api/compras/" + compraId).contentType("application/json")
                        .content(corpoCompra(fornecedorId, List.of(produtoId))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch("/api/compras/" + compraId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Tarde demais\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelamentoExigeMotivoENaoGeraEstoque() throws Exception {
        String fornecedorId = criarFornecedor("Fornecedor Cancelamento");
        String categoriaId = criarCategoria("Categoria Compra 4");
        String unidadeId = criarUnidadeMedida("Unidade Compra 4", "un");
        String produtoId = criarProduto("SKU-CMP-005", "Produto Compra 4", categoriaId, unidadeId);
        String compraId = criarCompra(fornecedorId, List.of(produtoId));

        mockMvc.perform(patch("/api/compras/" + compraId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\" \"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/compras/" + compraId + "/cancelar").contentType("application/json")
                        .content("{\"motivo\":\"Fornecedor sem estoque\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelamento").value("Fornecedor sem estoque"));

        mockMvc.perform(patch("/api/compras/" + compraId + "/receber"))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/estoque/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueFisico").value(0));
    }

    @Test
    void rejeitaFornecedorInativoEListaComFiltros() throws Exception {
        String fornecedorId = criarFornecedor("Fornecedor Inativo");
        String categoriaId = criarCategoria("Categoria Compra 5");
        String unidadeId = criarUnidadeMedida("Unidade Compra 5", "un");
        String produtoId = criarProduto("SKU-CMP-006", "Produto Compra 5", categoriaId, unidadeId);
        String compraId = criarCompra(fornecedorId, List.of(produtoId));

        mockMvc.perform(get("/api/compras").param("fornecedorId", fornecedorId).param("status", "RASCUNHO")
                        .param("dataInicio", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(compraId));

        mockMvc.perform(patch("/api/fornecedores/" + fornecedorId + "/status").contentType("application/json")
                        .content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/compras").contentType("application/json")
                        .content(corpoCompra(fornecedorId, List.of(produtoId))))
                .andExpect(status().isUnprocessableEntity());
    }
}
