package com.bella.backend.domain.usuario.model;

import java.security.SecureRandom;

/** Gera senhas aleatórias legíveis (sem letras e números que se confundem, como 0/O e 1/l/I). */
public final class SenhaProvisoria {

    private static final String ALFABETO = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 12;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SenhaProvisoria() {
    }

    /** Sempre com pelo menos uma letra e um número, para valer também na política de senha. */
    public static String gerar() {
        while (true) {
            StringBuilder sb = new StringBuilder(TAMANHO);
            for (int i = 0; i < TAMANHO; i++) {
                sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
            }
            String senha = sb.toString();
            if (senha.chars().anyMatch(Character::isDigit) && senha.chars().anyMatch(Character::isLetter)) {
                return senha;
            }
        }
    }
}
