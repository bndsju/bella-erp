package com.bella.backend.domain.usuario.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Regras da senha escolhida por uma pessoa: tamanho mínimo, letra e número, sem o login dentro e fora de uma lista
 * de senhas óbvias. Senhas provisórias geradas pelo sistema já nascem dentro dessas regras.
 */
public final class PoliticaDeSenha {

    public static final int TAMANHO_MINIMO = 10;
    /** Limite do BCrypt, que conta bytes e não caracteres. */
    public static final int TAMANHO_MAXIMO_BYTES = 72;

    private static final Set<String> COMUNS = Set.of(
            "1234567890", "123456789012", "12345678901", "0123456789", "senha12345", "senha123456", "senha@12345",
            "password123", "password1234", "password12", "qwerty12345", "qwertyuiop1", "abc1234567", "abcd123456",
            "admin12345", "admin123456", "bella12345", "bella123456", "mudar12345", "mudar123456", "brasil1234",
            "brasil12345", "senhasenha1", "iloveyou123", "welcome123", "letmein1234");

    private PoliticaDeSenha() {
    }

    public static void validar(String senha, String login) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new RegraDeNegocioException("A senha deve ter pelo menos " + TAMANHO_MINIMO + " caracteres");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO_BYTES) {
            throw new RegraDeNegocioException("A senha é longa demais (no máximo " + TAMANHO_MAXIMO_BYTES + " bytes)");
        }
        if (senha.chars().noneMatch(Character::isLetter) || senha.chars().noneMatch(Character::isDigit)) {
            throw new RegraDeNegocioException("A senha deve ter pelo menos uma letra e um número");
        }
        String normalizada = normalizar(senha);
        if (COMUNS.contains(normalizada) || todosIguais(senha)) {
            throw new RegraDeNegocioException("Essa senha é muito comum. Escolha outra");
        }
        if (login != null && login.length() >= 3 && normalizada.contains(normalizar(login))) {
            throw new RegraDeNegocioException("A senha não pode conter o seu login");
        }
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static boolean todosIguais(String senha) {
        return senha.chars().distinct().count() <= 2;
    }
}
