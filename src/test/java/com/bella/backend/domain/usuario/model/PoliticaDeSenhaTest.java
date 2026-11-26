package com.bella.backend.domain.usuario.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticaDeSenhaTest {

    @Test
    void aceitaSenhaBoa() {
        assertThatCode(() -> PoliticaDeSenha.validar("cafe-com-leite-42", "maria")).doesNotThrowAnyException();
    }

    @Test
    void recusaSenhaCurtaSemNumeroOuSemLetra() {
        assertThatThrownBy(() -> PoliticaDeSenha.validar("abc123", "maria")).hasMessageContaining("pelo menos 10");
        assertThatThrownBy(() -> PoliticaDeSenha.validar("somente-letras", "maria")).hasMessageContaining("letra e um número");
        assertThatThrownBy(() -> PoliticaDeSenha.validar("1234567890123", "maria")).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void recusaSenhaComumRepetidaOuComOLogin() {
        assertThatThrownBy(() -> PoliticaDeSenha.validar("Senha12345", "maria")).hasMessageContaining("comum");
        assertThatThrownBy(() -> PoliticaDeSenha.validar("aaaaaaaaa1", "maria")).hasMessageContaining("comum");
        assertThatThrownBy(() -> PoliticaDeSenha.validar("MARIA-2026-xx", "maria")).hasMessageContaining("login");
    }

    @Test
    void recusaSenhaMaiorQueOLimiteDoBcrypt() {
        assertThatThrownBy(() -> PoliticaDeSenha.validar("a1".repeat(40), "maria")).hasMessageContaining("longa demais");
    }

    @Test
    void senhaProvisoriaGeradaSempreAtendeAPolitica() {
        for (int i = 0; i < 500; i++) {
            String senha = SenhaProvisoria.gerar();
            assertThatCode(() -> PoliticaDeSenha.validar(senha, "usuario")).doesNotThrowAnyException();
        }
    }
}
