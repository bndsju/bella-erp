package com.bella.backend.domain.cliente.validation;

import com.bella.backend.domain.shared.RegraDeNegocioException;

/**
 * Validação de CPF por dígito verificador (módulo 11), conforme algoritmo da Receita Federal.
 */
public final class CpfValidator {

    private static final int[] PESOS_PRIMEIRO_DIGITO = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

    private CpfValidator() {
    }

    /**
     * Remove a máscara, valida o dígito verificador e retorna o CPF só com dígitos.
     */
    public static String normalizarEValidar(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");

        if (digitos.length() != 11 || todosDigitosIguais(digitos)) {
            throw new RegraDeNegocioException("CPF inválido");
        }

        int primeiroDigito = calcularDigito(digitos, PESOS_PRIMEIRO_DIGITO);
        int segundoDigito = calcularDigito(digitos.substring(0, 9) + primeiroDigito, PESOS_SEGUNDO_DIGITO);

        if (!digitos.endsWith("" + primeiroDigito + segundoDigito)) {
            throw new RegraDeNegocioException("CPF inválido");
        }

        return digitos;
    }

    private static int calcularDigito(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosDigitosIguais(String digitos) {
        return digitos.chars().distinct().count() == 1;
    }
}
