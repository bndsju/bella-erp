package com.bella.backend.domain.cliente.validation;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfValidatorTest {

    @Test
    void aceitaCpfValidoComMascara() {
        assertThat(CpfValidator.normalizarEValidar("111.444.777-35")).isEqualTo("11144477735");
    }

    @Test
    void aceitaCpfValidoSemMascara() {
        assertThat(CpfValidator.normalizarEValidar("11144477735")).isEqualTo("11144477735");
    }

    @Test
    void rejeitaDigitoVerificadorInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> CpfValidator.normalizarEValidar("11144477736"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
    void rejeitaSequenciasComTodosDigitosIguais(String cpf) {
        assertThrows(RegraDeNegocioException.class, () -> CpfValidator.normalizarEValidar(cpf));
    }

    @Test
    void rejeitaTamanhoInvalido() {
        assertThrows(RegraDeNegocioException.class, () -> CpfValidator.normalizarEValidar("123"));
    }

    @Test
    void rejeitaNulo() {
        assertThrows(RegraDeNegocioException.class, () -> CpfValidator.normalizarEValidar(null));
    }
}
