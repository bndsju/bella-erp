package com.bella.backend.adapter.in.web.permissao;

import com.bella.backend.IntegrationTestBase;
import com.bella.backend.domain.permissao.model.PermissoesPadrao;
import com.bella.backend.domain.permissao.port.in.ConsultarPermissoesUseCase;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Matriz de permissões por perfil: edição pelo administrador e efeito imediato sobre a API. */
@TestPropertySource(properties = {
        "bella.seguranca.habilitada=true",
        "bella.seguranca.admin-login=admin",
        "bella.seguranca.admin-senha=senha-do-admin-1",
        "bella.seguranca.jwt-segredo=segredo-de-teste-com-mais-de-32-caracteres"
})
class PermissaoIntegrationTest extends IntegrationTestBase {

    private static final String SENHA_ADMIN = "senha-do-admin-1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ConsultarPermissoesUseCase permissoes;

    private String admin;

    private String token(String login, String senha) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("login", login, "senha", senha))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String adminToken() throws Exception {
        if (admin == null) {
            admin = token("admin", SENHA_ADMIN);
        }
        return admin;
    }

    private String usuarioComPerfil(String login, String perfil) throws Exception {
        mockMvc.perform(post("/api/usuarios").header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Usuário " + login, "login", login, "senha", "senha-12345", "perfil", perfil))))
                .andExpect(status().isCreated());
        return token(login, "senha-12345");
    }

    private ResultActions definir(String perfil, String corpoJson) throws Exception {
        return mockMvc.perform(put("/api/permissoes/" + perfil).header("Authorization", bearer(adminToken()))
                .contentType(MediaType.APPLICATION_JSON).content(corpoJson));
    }

    @AfterEach
    void restaurar() throws Exception {
        for (String perfil : new String[]{"GERENTE", "FINANCEIRO", "VENDEDOR"}) {
            mockMvc.perform(post("/api/permissoes/" + perfil + "/restaurar-padrao")
                    .header("Authorization", bearer(adminToken()))).andExpect(status().isOk());
        }
    }

    @Test
    void asPermissoesIniciaisDoBancoSaoOPadraoDoCodigo() {
        for (PerfilUsuario perfil : PerfilUsuario.values()) {
            assertThat(permissoes.doPerfil(perfil)).as(perfil.name()).isEqualTo(PermissoesPadrao.de(perfil));
        }
    }

    @Test
    void somenteOAdministradorVeOuAlteraAMatriz() throws Exception {
        String gerente = usuarioComPerfil("gerente1", "GERENTE");

        mockMvc.perform(get("/api/permissoes").header("Authorization", bearer(gerente))).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/permissoes/VENDEDOR").header("Authorization", bearer(gerente))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"permissoes\":{}}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/permissoes")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/permissoes").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfis.length()").value(4))
                .andExpect(jsonPath("$.modulos[?(@.chave=='VENDAS')].acoes[*]").value(
                        org.hamcrest.Matchers.containsInAnyOrder("VER", "CRIAR", "CANCELAR")))
                .andExpect(jsonPath("$.perfis[?(@.perfil=='ADMINISTRADOR')].fixo").value(org.hamcrest.Matchers.contains(true)));
    }

    @Test
    void concederPermissaoValeNaHoraENaoLiberaOutrasAcoes() throws Exception {
        String vendedor = usuarioComPerfil("vend1", "VENDEDOR");
        mockMvc.perform(get("/api/contas-pagar").header("Authorization", bearer(vendedor))).andExpect(status().isForbidden());

        definir("VENDEDOR", "{\"permissoes\":{\"CONTAS_PAGAR\":[\"VER\"],\"CLIENTES\":[\"VER\"]}}")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/contas-pagar").header("Authorization", bearer(vendedor))).andExpect(status().isOk());
        mockMvc.perform(post("/api/contas-pagar").header("Authorization", bearer(vendedor))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void retirarPermissaoBloqueiaNaHora() throws Exception {
        String vendedor = usuarioComPerfil("vend2", "VENDEDOR");
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(vendedor))).andExpect(status().isOk());

        definir("VENDEDOR", "{\"permissoes\":{\"PRODUTOS\":[\"VER\"]}}").andExpect(status().isOk());

        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(vendedor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/produtos").header("Authorization", bearer(vendedor))).andExpect(status().isOk());
    }

    @Test
    void criarEditarOuCancelarSempreTrazemOVer() throws Exception {
        JsonNode resposta = objectMapper.readTree(definir("FINANCEIRO", "{\"permissoes\":{\"CLIENTES\":[\"EDITAR\"]}}")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        assertThat(resposta.get("permissoes").get("CLIENTES")).extracting(JsonNode::asText)
                .containsExactlyInAnyOrder("VER", "EDITAR");
    }

    @Test
    void recusaAdministradorModuloDesconhecidoEAcaoQueOModuloNaoTem() throws Exception {
        definir("ADMINISTRADOR", "{\"permissoes\":{}}").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/permissoes/ADMINISTRADOR/restaurar-padrao")
                .header("Authorization", bearer(adminToken()))).andExpect(status().isUnprocessableEntity());
        definir("VENDEDOR", "{\"permissoes\":{\"INEXISTENTE\":[\"VER\"]}}").andExpect(status().isBadRequest());
        definir("VENDEDOR", "{\"permissoes\":{\"ESTOQUE\":[\"CANCELAR\"]}}").andExpect(status().isUnprocessableEntity());
        definir("VENDEDOR", "{}").andExpect(status().isBadRequest());
    }

    @Test
    void usuariosEPermissoesNuncaSaoConcedidosAOutrosPerfis() throws Exception {
        String gerente = usuarioComPerfil("gerente2", "GERENTE");
        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(gerente))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/permissoes").header("Authorization", bearer(gerente))).andExpect(status().isForbidden());
    }

    @Test
    void restaurarPadraoDesfazAsAlteracoes() throws Exception {
        String vendedor = usuarioComPerfil("vend3", "VENDEDOR");
        definir("VENDEDOR", "{\"permissoes\":{}}").andExpect(status().isOk());
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(vendedor))).andExpect(status().isForbidden());

        mockMvc.perform(post("/api/permissoes/VENDEDOR/restaurar-padrao").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(vendedor))).andExpect(status().isOk());
        assertThat(permissoes.doPerfil(PerfilUsuario.VENDEDOR)).isEqualTo(PermissoesPadrao.de(PerfilUsuario.VENDEDOR));
    }

    @Test
    void minhasPermissoesRefletemOPerfilELimitamAsTelas() throws Exception {
        String vendedor = usuarioComPerfil("vend4", "VENDEDOR");

        mockMvc.perform(get("/api/auth/permissoes").header("Authorization", bearer(vendedor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissoes.CLIENTES.length()").value(4))
                .andExpect(jsonPath("$.permissoes.CONTAS_PAGAR").doesNotExist());
        mockMvc.perform(get("/api/auth/permissoes").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissoes.AUDITORIA[0]").value("VER"));
    }
}
