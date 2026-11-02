package com.bella.backend.adapter.in.web.auditoria;

import com.bella.backend.IntegrationTestBase;
import com.bella.backend.adapter.out.persistence.auditoria.AuditoriaRepositoryAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A auditoria participa da transação da operação: se não puder ser gravada, a operação não vale. */
class AuditoriaTransacionalIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private AuditoriaRepositoryAdapter auditoriaRepositoryAdapter;

    @Test
    void falhaAoGravarAAuditoriaDesfazAOperacao() throws Exception {
        doThrow(new IllegalStateException("falha simulada na auditoria"))
                .when(auditoriaRepositoryAdapter).salvar(any());

        Map<String, Object> endereco = new LinkedHashMap<>();
        endereco.put("cep", "01001000");
        endereco.put("logradouro", "Rua A");
        endereco.put("numero", "10");
        endereco.put("bairro", "Centro");
        endereco.put("cidade", "Sao Paulo");
        endereco.put("uf", "SP");
        Map<String, Object> cliente = new LinkedHashMap<>();
        cliente.put("tipoPessoa", "PF");
        cliente.put("nomeCompleto", "Cliente Desfeito");
        cliente.put("dataNascimento", "1990-05-10");
        cliente.put("cpf", "111.444.777-35");
        cliente.put("celular", "11999998888");
        cliente.put("email", "desfeito@example.com");
        cliente.put("endereco", endereco);

        assertThatThrownBy(() -> mockMvc.perform(post("/api/clientes").contentType("application/json")
                .content(objectMapper.writeValueAsString(cliente))))
                .hasRootCauseMessage("falha simulada na auditoria");

        String clientes = mockMvc.perform(get("/api/clientes")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(clientes)).isEmpty();
    }
}
