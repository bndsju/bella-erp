package com.bella.backend.adapter.out.contexto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRequisicaoAdapterTest {

    private final UsuarioRequisicaoAdapter adapter = new UsuarioRequisicaoAdapter();

    @AfterEach
    void limpar() {
        RequestContextHolder.resetRequestAttributes();
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
}
