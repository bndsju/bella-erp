package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "bella.seguranca.habilitada=true",
        "bella.seguranca.admin-senha=senha-do-chefe-1",
        "bella.seguranca.jwt-segredo=segredo-de-teste-com-mais-de-32-caracteres",
        "bella.seguranca.limite.login-por-ip=6",
        "bella.seguranca.limite.esqueci-senha-por-ip=3"
})
class LimiteRequisicoesIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void esqueciSenhaTemLimitePorIp() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/esqueci-senha").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"login\":\"alguem" + i + "\"}")).andExpect(status().isAccepted());
        }
        mockMvc.perform(post("/api/auth/esqueci-senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"alguem9\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void loginTemLimitePorIpMesmoComLoginsDiferentes() throws Exception {
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"login\":\"robo" + i + "\",\"senha\":\"tentativa-1\"}")).andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"robo9\",\"senha\":\"tentativa-1\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
