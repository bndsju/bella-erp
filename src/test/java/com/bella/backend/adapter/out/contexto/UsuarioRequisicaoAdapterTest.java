package com.bella.backend.adapter.out.contexto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.bella.backend.adapter.in.web.seguranca.UsuarioAutenticado;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRequisicaoAdapterTest {

    private final UsuarioRequisicaoAdapter adapter = new UsuarioRequisicaoAdapter(false);

    @AfterEach
    void limpar() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    private void comCabecalho(String valor) {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        if (valor != null) {
            requisicao.addHeader(UsuarioRequisicaoAdapter.CABECALHO, valor);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requisicao));
    }

    @Test
    void foraDeUmaRequisicaoOResponsavelESistema() {
        assertThat(adapter.usuarioAtual()).isEqualTo("sistema");
    }

    @Test
    void usaOCabecalhoQuandoInformado() {
        comCabecalho("  ana.souza ");

        assertThat(adapter.usuarioAtual()).isEqualTo("ana.souza");
    }

    @Test
    void cabecalhoAusenteOuVazioViraSistema() {
        comCabecalho(null);
        assertThat(adapter.usuarioAtual()).isEqualTo("sistema");

        comCabecalho("   ");
        assertThat(adapter.usuarioAtual()).isEqualTo("sistema");
    }

    @Test
    void removeCaracteresDeControleELimitaOTamanho() {
        comCabecalho("ana\tsouza" + "x".repeat(200));

        assertThat(adapter.usuarioAtual()).startsWith("anasouza").hasSize(100);
    }

    private void autenticarComo(String login) {
        var principal = new UsuarioAutenticado(UUID.randomUUID(), login, "Nome", PerfilUsuario.VENDEDOR);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    void usuarioAutenticadoTemPrioridadeSobreOCabecalho() {
        autenticarComo("maria");
        comCabecalho("forjado");

        assertThat(adapter.usuarioAtual()).isEqualTo("maria");
    }

    @Test
    void comSegurancaLigadaOCabecalhoEIgnorado() {
        UsuarioRequisicaoAdapter seguro = new UsuarioRequisicaoAdapter(true);
        comCabecalho("forjado");

        assertThat(seguro.usuarioAtual()).isEqualTo("sistema");
    }
}
