package com.bella.backend.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converte a condição de pagamento (texto livre de Pedido/Compra) em parcelas.
 * "30/60/90 dias" gera parcelas a 30, 60 e 90 dias da data base; "3x" ou "3 parcelas" gera três
 * parcelas mensais; "à vista" ou qualquer texto não reconhecido gera uma única parcela na data base.
 * A última parcela absorve o resto da divisão em centavos.
 */
public final class CondicaoPagamentoParcelas {

    public record Parcela(BigDecimal valor, LocalDate vencimento) {
    }

    private static final Pattern VEZES = Pattern.compile("(\\d{1,2})\\s*(x|vezes|parcelas?)\\b");
    private static final Pattern NUMERO = Pattern.compile("\\d{1,3}");
    private static final int MAXIMO_PARCELAS = 60;

    private CondicaoPagamentoParcelas() {
    }

    public static List<Parcela> gerar(String condicao, BigDecimal valorTotal, LocalDate dataBase) {
        List<LocalDate> vencimentos = vencimentos(normalizar(condicao), dataBase);
        long totalCentavos = valorTotal.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        long base = totalCentavos / vencimentos.size();

        List<Parcela> parcelas = new ArrayList<>();
        long acumulado = 0;
        for (int i = 0; i < vencimentos.size(); i++) {
            long centavos = i == vencimentos.size() - 1 ? totalCentavos - acumulado : base;
            acumulado += centavos;
            parcelas.add(new Parcela(BigDecimal.valueOf(centavos, 2), vencimentos.get(i)));
        }
        return parcelas;
    }

    private static List<LocalDate> vencimentos(String condicao, LocalDate dataBase) {
        Matcher vezes = VEZES.matcher(condicao);
        if (vezes.find()) {
            int quantidade = Math.max(1, Math.min(Integer.parseInt(vezes.group(1)), MAXIMO_PARCELAS));
            List<LocalDate> datas = new ArrayList<>();
            for (int i = 1; i <= quantidade; i++) {
                datas.add(dataBase.plusMonths(i));
            }
            return datas;
        }
        List<LocalDate> datas = new ArrayList<>();
        Matcher numero = NUMERO.matcher(condicao);
        while (numero.find() && datas.size() < MAXIMO_PARCELAS) {
            datas.add(dataBase.plusDays(Integer.parseInt(numero.group())));
        }
        return datas.isEmpty() ? List.of(dataBase) : datas;
    }

    private static String normalizar(String condicao) {
        if (condicao == null) {
            return "";
        }
        return Normalizer.normalize(condicao, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }
}
