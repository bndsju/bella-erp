package com.bella.backend.domain.cliente.validation;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CnpjValidatorTest {

    @Test
    void aceitaCnpjValidoComMascara() {
        assertThat(CnpjValidator.normalizarEValidar("11.222.333/0001-81")).isEqualTo("11222333000181");
    }

    @Test
    void aceitaCnpjValidoSemMascara() {
        assertThat(CnpjValidator.normalizarEValidar("11222333000181")).isEqualTo("11222333000181");
    }

    @Test
    void rejeitaDigitoVerificadorInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> CnpjValidator.normalizarEValidar("11222333000182"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000000", "11111111111111"})
    void rejeitaSequenciasComTodosDigitosIguais(String cnpj) {
        assertThrows(RegraDeNegocioException.class, () -> CnpjValidator.normalizarEValidar(cnpj));
    }

    @Test
    void rejeitaTamanhoInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> CnpjValidator.normalizarEValidar("123"));
    }
}
