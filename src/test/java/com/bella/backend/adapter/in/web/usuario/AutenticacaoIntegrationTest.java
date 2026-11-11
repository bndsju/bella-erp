package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Segurança ligada de verdade: o resto dos testes de integração roda com ela desligada. */
@TestPropertySource(properties = {
        "bella.seguranca.habilitada=true",
        "bella.seguranca.admin-login=admin",
        "bella.seguranca.admin-senha=senha-do-admin-1",
        "bella.seguranca.jwt-segredo=segredo-de-teste-com-mais-de-32-caracteres"
})
class AutenticacaoIntegrationTest extends IntegrationTestBase {

    private static final String SENHA_ADMIN = "senha-do-admin-1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions login(String login, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("login", login, "senha", senha))));
    }

    private String token(String login, String senha) throws Exception {
        String corpo = login(login, senha).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private JsonNode criarUsuario(String tokenAdmin, String login, String perfil) throws Exception {
        String corpo = mockMvc.perform(post("/api/usuarios").header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Usuário " + login, "login", login, "senha", "senha-12345", "perfil", perfil))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo);
    }

    @Test
    void semTokenOuComTokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").exists());
        mockMvc.perform(get("/api/clientes").header("Authorization", "Bearer lixo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComCredenciaisCertasDaAcessoEResponde() throws Exception {
        login("admin", SENHA_ADMIN).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.perfil").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist())
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist());

        String token = token("ADMIN", SENHA_ADMIN); // o login não diferencia maiúsculas
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.login").value("admin"));
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(token))).andExpect(status().isOk());
    }

    @Test
    void loginErradoRetorna401ComMensagemUnica() throws Exception {
        login("admin", "senha-errada").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"));
        login("nao-existe", "qualquer-senha").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"));
    }

    @Test
    void cincoSenhasErradasBloqueiamOLogin() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("bloqueado", "errada-" + i).andExpect(status().isUnauthorized());
        }
        login("bloqueado", "errada-final").andExpect(status().isTooManyRequests());
    }

    @Test
    void perfilDeVendedorNaoAcessaUsuariosFinanceiroNemAuditoria() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        criarUsuario(admin, "vendedor1", "VENDEDOR");
        String vendedor = token("vendedor1", "senha-12345");

        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(vendedor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/contas-pagar").header("Authorization", bearer(vendedor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/auditoria").header("Authorization", bearer(vendedor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(vendedor))).andExpect(status().isOk());
    }

    @Test
    void financeiroAcessaContasMasNaoUsuarios() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        criarUsuario(admin, "financeiro1", "FINANCEIRO");
        String financeiro = token("financeiro1", "senha-12345");

        mockMvc.perform(get("/api/contas-receber").header("Authorization", bearer(financeiro))).andExpect(status().isOk());
        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(financeiro))).andExpect(status().isForbidden());
    }

    @Test
    void cadastroRejeitaLoginRepetidoSenhaCurtaELoginInvalido() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        criarUsuario(admin, "maria", "GERENTE");

        mockMvc.perform(post("/api/usuarios").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Outra Maria", "login", "MARIA", "senha", "senha-12345", "perfil", "VENDEDOR"))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/usuarios").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Curta", "login", "curta", "senha", "123", "perfil", "VENDEDOR"))))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/usuarios").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Espaço", "login", "com espaco", "senha", "senha-12345", "perfil", "VENDEDOR"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void usuarioInativadoPerdeOAcessoNaHora() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        String id = criarUsuario(admin, "temporario", "VENDEDOR").get("id").asText();
        String tokenTemporario = token("temporario", "senha-12345");
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(tokenTemporario))).andExpect(status().isOk());

        mockMvc.perform(patch("/api/usuarios/" + id + "/status").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(tokenTemporario)))
                .andExpect(status().isUnauthorized());
        login("temporario", "senha-12345").andExpect(status().isUnauthorized());
    }

    @Test
    void administradorNaoPodeSeInativarNemRebaixar() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        String meuId = objectMapper.readTree(mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/usuarios/" + meuId + "/status").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(put("/api/usuarios/" + meuId).header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Admin\",\"perfil\":\"VENDEDOR\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void trocarAPropriaSenhaExigeSenhaAtualEInvalidaTokensAntigos() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        criarUsuario(admin, "carlos", "GERENTE");
        String tokenCarlos = token("carlos", "senha-12345");

        mockMvc.perform(put("/api/auth/senha").header("Authorization", bearer(tokenCarlos))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"errada-errada\",\"novaSenha\":\"nova-senha-123\"}"))
                .andExpect(status().isUnprocessableEntity());

        // a troca de senha derruba o token emitido antes dela: espera virar o segundo para o iat do token ficar anterior
        Thread.sleep(1100);
        mockMvc.perform(put("/api/auth/senha").header("Authorization", bearer(tokenCarlos))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"senha-12345\",\"novaSenha\":\"nova-senha-123\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(tokenCarlos)))
                .andExpect(status().isUnauthorized());
        login("carlos", "senha-12345").andExpect(status().isUnauthorized());
        login("carlos", "nova-senha-123").andExpect(status().isOk());
    }

    private ResultActions esqueciSenha(String login) throws Exception {
        return mockMvc.perform(post("/api/auth/esqueci-senha").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("login", login))));
    }

    private JsonNode usuarioNaLista(String tokenAdmin, String id) throws Exception {
        String corpo = mockMvc.perform(get("/api/usuarios/" + id).header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo);
    }

    @Test
    void esqueciASenhaResponde202IgualParaLoginExistenteOuNao() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        criarUsuario(admin, "esquecida", "VENDEDOR");

        String existente = esqueciSenha("esquecida").andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        String inexistente = esqueciSenha("ninguem-assim").andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(existente).isEqualTo(inexistente);
        esqueciSenha("").andExpect(status().isBadRequest());
    }

    @Test
    void pedidoFicaMarcadoParaOAdministradorEUsuarioInativoNaoGeraPedido() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        String id = criarUsuario(admin, "pediu", "VENDEDOR").get("id").asText();
        String idInativo = criarUsuario(admin, "inativa", "VENDEDOR").get("id").asText();
        mockMvc.perform(patch("/api/usuarios/" + idInativo + "/status").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INATIVO\"}")).andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(usuarioNaLista(admin, id).get("redefinicaoSolicitadaEm").isNull()).isTrue();
        esqueciSenha("pediu").andExpect(status().isAccepted());
        esqueciSenha("inativa").andExpect(status().isAccepted());

        org.assertj.core.api.Assertions.assertThat(usuarioNaLista(admin, id).get("redefinicaoSolicitadaEm").isNull()).isFalse();
        org.assertj.core.api.Assertions.assertThat(usuarioNaLista(admin, idInativo).get("redefinicaoSolicitadaEm").isNull()).isTrue();
    }

    @Test
    void senhaProvisoriaObrigaATrocarAntesDeUsarOSistema() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        String id = criarUsuario(admin, "esqueceu", "VENDEDOR").get("id").asText();
        esqueciSenha("esqueceu").andExpect(status().isAccepted());

        String resposta = mockMvc.perform(post("/api/usuarios/" + id + "/senha-provisoria")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.senhaProvisoria").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String provisoria = objectMapper.readTree(resposta).get("senhaProvisoria").asText();
        org.assertj.core.api.Assertions.assertThat(usuarioNaLista(admin, id).get("redefinicaoSolicitadaEm").isNull()).isTrue();

        login("esqueceu", "senha-12345").andExpect(status().isUnauthorized()); // a antiga morreu
        String corpoLogin = login("esqueceu", provisoria).andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.senhaProvisoria").value(true))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(corpoLogin).get("token").asText();

        // com a provisória só dá para ver quem sou e trocar a senha
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.mensagem").value("Defina uma nova senha para continuar"));
        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token))).andExpect(status().isOk());

        mockMvc.perform(put("/api/auth/senha").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("senhaAtual", provisoria, "novaSenha", "minha-senha-nova-1"))))
                .andExpect(status().isNoContent());

        login("esqueceu", provisoria).andExpect(status().isUnauthorized());
        String novo = token("esqueceu", "minha-senha-nova-1");
        mockMvc.perform(get("/api/clientes").header("Authorization", bearer(novo))).andExpect(status().isOk());
        org.assertj.core.api.Assertions.assertThat(usuarioNaLista(admin, id).get("senhaProvisoria").asBoolean()).isFalse();
    }

    @Test
    void senhaProvisoriaNaoServeParaSiMesmoNemParaInativoNemParaNaoAdministrador() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        String meuId = objectMapper.readTree(mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString()).get("id").asText();
        mockMvc.perform(post("/api/usuarios/" + meuId + "/senha-provisoria").header("Authorization", bearer(admin)))
                .andExpect(status().isUnprocessableEntity());

        String idInativo = criarUsuario(admin, "parada", "VENDEDOR").get("id").asText();
        mockMvc.perform(patch("/api/usuarios/" + idInativo + "/status").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INATIVO\"}")).andExpect(status().isOk());
        mockMvc.perform(post("/api/usuarios/" + idInativo + "/senha-provisoria").header("Authorization", bearer(admin)))
                .andExpect(status().isUnprocessableEntity());

        criarUsuario(admin, "curiosa", "GERENTE");
        String gerente = token("curiosa", "senha-12345");
        mockMvc.perform(post("/api/usuarios/" + idInativo + "/senha-provisoria").header("Authorization", bearer(gerente)))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditoriaRegistraOLoginAutenticadoEIgnoraOCabecalhoForjado() throws Exception {
        String admin = token("admin", SENHA_ADMIN);
        criarUsuario(admin, "operador", "VENDEDOR");
        String operador = token("operador", "senha-12345");

        String corpoCliente = "{\"tipoPessoa\":\"PF\",\"nomeCompleto\":\"Cliente Teste\",\"dataNascimento\":\"1990-05-10\","
                + "\"cpf\":\"52998224725\",\"celular\":\"11999999999\",\"email\":\"teste@exemplo.com\","
                + "\"endereco\":{\"cep\":\"01001000\",\"logradouro\":\"Rua A\",\"numero\":\"10\",\"bairro\":\"Centro\","
                + "\"cidade\":\"Sao Paulo\",\"uf\":\"SP\"}}";
        mockMvc.perform(post("/api/clientes").header("Authorization", bearer(operador)).header("X-Usuario", "forjado")
                        .contentType(MediaType.APPLICATION_JSON).content(corpoCliente))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/auditoria").param("usuario", "operador").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(1));
        mockMvc.perform(get("/api/auditoria").param("usuario", "forjado").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalItens").value(0));
    }
}
