package com.bella.backend.application.auditoria;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Converte registros em instantâneos comparáveis, remove dados sensíveis e calcula o que mudou entre
 * dois estados. Campos puramente técnicos (como a data da última atualização) não contam como mudança.
 */
@Component
public class SnapshotAuditoria {

    private static final Pattern CAMPO_SENSIVEL =
            Pattern.compile(".*(senha|password|passwd|token|secret|segredo|apikey|api_key|credencial).*",
                    Pattern.CASE_INSENSITIVE);
    private static final Set<String> CAMPOS_TECNICOS = Set.of("atualizadoEm");
    private static final TypeReference<LinkedHashMap<String, Object>> TIPO_MAPA = new TypeReference<>() {
    };

    /** Campos que mudaram: o valor antigo de cada um e o novo valor de cada um. */
    public record Diferenca(Map<String, Object> anteriores, Map<String, Object> novos) {

        public boolean vazia() {
            return novos.isEmpty() && anteriores.isEmpty();
        }
    }

    private final ObjectMapper objectMapper;

    public SnapshotAuditoria(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode paraNo(Object registro) {
        JsonNode no = objectMapper.valueToTree(registro);
        if (!(no instanceof ObjectNode objeto)) {
            throw new IllegalArgumentException("Só é possível auditar registros com campos nomeados");
        }
        removerSensiveis(objeto);
        return objeto;
    }

    public Map<String, Object> paraMapa(ObjectNode no) {
        return objectMapper.convertValue(no, TIPO_MAPA);
    }

    public Diferenca diferenca(ObjectNode antes, ObjectNode depois) {
        ObjectNode anteriores = objectMapper.createObjectNode();
        ObjectNode novos = objectMapper.createObjectNode();

        Iterator<String> campos = depois.fieldNames();
        while (campos.hasNext()) {
            String campo = campos.next();
            if (CAMPOS_TECNICOS.contains(campo)) {
                continue;
            }
            JsonNode valorNovo = depois.get(campo);
            JsonNode valorAntigo = antes.get(campo);
            if (valorAntigo == null || !valorAntigo.equals(valorNovo)) {
                novos.set(campo, valorNovo);
                anteriores.set(campo, valorAntigo == null ? objectMapper.nullNode() : valorAntigo);
            }
        }
        Iterator<String> camposAntigos = antes.fieldNames();
        while (camposAntigos.hasNext()) {
            String campo = camposAntigos.next();
            if (!CAMPOS_TECNICOS.contains(campo) && !depois.has(campo)) {
                anteriores.set(campo, antes.get(campo));
                novos.set(campo, objectMapper.nullNode());
            }
        }
        return new Diferenca(paraMapa(anteriores), paraMapa(novos));
    }

    private void removerSensiveis(JsonNode no) {
        if (no instanceof ObjectNode objeto) {
            objeto.fieldNames().forEachRemaining(campo -> {
                if (CAMPO_SENSIVEL.matcher(campo).matches()) {
                    objeto.put(campo, "***");
                } else {
                    removerSensiveis(objeto.get(campo));
                }
            });
        } else if (no != null && no.isArray()) {
            no.forEach(this::removerSensiveis);
        }
    }
}
