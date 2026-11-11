package com.bella.backend.domain.usuario.model;

import java.security.SecureRandom;

/** Gera senhas aleatórias legíveis (sem letras e números que se confundem, como 0/O e 1/l/I). */
public final class SenhaProvisoria {

    private static final String ALFABETO = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 12;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaProvisoria() {
    }

    public static String gerar() {
        StringBuilder sb = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
