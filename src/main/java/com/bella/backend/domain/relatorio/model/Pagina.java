package com.bella.backend.domain.relatorio.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;

import java.util.List;

/** Página de resultados de um relatório. Páginas começam em 0. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 200;

    public static void validar(int pagina, int tamanho) {
        if (pagina < 0) {
            throw new RegraDeNegocioException("A página deve ser maior ou igual a zero");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new RegraDeNegocioException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO);
        }
    }

    public static <T> Pagina<T> de(List<T> todos, int pagina, int tamanho) {
        validar(pagina, tamanho);
        long total = todos.size();
        int de = (int) Math.min((long) pagina * tamanho, total);
        int ate = (int) Math.min((long) de + tamanho, total);
        int totalPaginas = (int) Math.ceil(total / (double) tamanho);
        return new Pagina<>(List.copyOf(todos.subList(de, ate)), pagina, tamanho, total, totalPaginas);
    }
}
