package com.bella.backend.domain.cliente.validation;

import com.bella.backend.domain.shared.RegraDeNegocioException;

/**
 * Validação de CNPJ por dígito verificador (módulo 11), conforme algoritmo da Receita Federal.
 */
public final class CnpjValidator {

    private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private CnpjValidator() {
    }

    /**
     * Remove a máscara, valida o dígito verificador e retorna o CNPJ só com dígitos.
     */
    public static String normalizarEValidar(String cnpj) {
        String digitos = cnpj == null ? "" : cnpj.replaceAll("\\D", "");

        if (digitos.length() != 14 || todosDigitosIguais(digitos)) {
            throw new RegraDeNegocioException("CNPJ inválido");
        }

        int primeiroDigito = calcularDigito(digitos, PESOS_PRIMEIRO_DIGITO);
        int segundoDigito = calcularDigito(digitos.substring(0, 12) + primeiroDigito, PESOS_SEGUNDO_DIGITO);

        if (!digitos.endsWith("" + primeiroDigito + segundoDigito)) {
            throw new RegraDeNegocioException("CNPJ inválido");
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
