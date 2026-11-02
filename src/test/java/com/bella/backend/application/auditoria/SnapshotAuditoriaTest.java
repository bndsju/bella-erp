package com.bella.backend.application.auditoria;

import com.bella.backend.application.auditoria.SnapshotAuditoria.Diferenca;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotAuditoriaTest {

    private final SnapshotAuditoria snapshots = new SnapshotAuditoria(new ObjectMapper().registerModule(new JavaTimeModule()));

    public record Exemplo(String nome, BigDecimal valor, String senha, Map<String, String> acesso,
                          List<String> itens, Instant atualizadoEm) {
    }

    private ObjectNode no(String nome, String valor, String senha, String token, List<String> itens) {
        return snapshots.paraNo(new Exemplo(nome, new BigDecimal(valor), senha,
                Map.of("accessToken", token, "usuario", "ana"), itens, Instant.now()));
    }

    @Test
    void dadosSensiveisNuncaSaemNoInstantaneo() {
        ObjectNode no = no("Teste", "10.00", "segredo123", "abc.def.ghi", List.of("a"));

        assertThat(no.get("senha").asText()).isEqualTo("***");
        assertThat(no.get("acesso").get("accessToken").asText()).isEqualTo("***");
        assertThat(no.get("acesso").get("usuario").asText()).isEqualTo("ana");
        assertThat(snapshots.paraMapa(no).toString()).doesNotContain("segredo123").doesNotContain("abc.def.ghi");
    }

    @Test
    void diferencaTrazSomenteOsCamposQueMudaram() {
        ObjectNode antes = no("Teste", "10.00", "x", "t", List.of("a"));
        ObjectNode depois = no("Teste", "12.50", "x", "t", List.of("a", "b"));

        Diferenca diferenca = snapshots.diferenca(antes, depois);

        assertThat(diferenca.anteriores()).containsOnlyKeys("valor", "itens");
        assertThat(diferenca.novos()).containsOnlyKeys("valor", "itens");
        assertThat(diferenca.novos().get("itens")).isEqualTo(List.of("a", "b"));
    }

    @Test
    void camposTecnicosEEstadosIguaisNaoContamComoMudanca() {
        ObjectNode antes = no("Teste", "10.00", "x", "t", List.of("a"));
        ObjectNode depois = no("Teste", "10.0", "x", "t", List.of("a"));

        assertThat(snapshots.diferenca(antes, depois).vazia()).isTrue();
    }
}
