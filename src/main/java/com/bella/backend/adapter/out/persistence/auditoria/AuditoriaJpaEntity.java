package com.bella.backend.adapter.out.persistence.auditoria;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.auditoria.model.OperacaoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Registro de auditoria: imutável também para o Hibernate (nunca gera UPDATE nem DELETE). */
@Entity
@Immutable
@Table(name = "auditorias", schema = "bella")
public class AuditoriaJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModuloAuditoria modulo;

    @Column(name = "entidade_id", nullable = false)
    private UUID entidadeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OperacaoAuditoria operacao;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_anteriores")
    private Map<String, Object> valoresAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "novos_valores")
    private Map<String, Object> novosValores;

    @Column(length = 1000)
    private String motivo;

    protected AuditoriaJpaEntity() {
    }

    public AuditoriaJpaEntity(UUID id, ModuloAuditoria modulo, UUID entidadeId, OperacaoAuditoria operacao,
                              String usuario, Instant dataHora, Map<String, Object> valoresAnteriores,
                              Map<String, Object> novosValores, String motivo) {
        this.id = id;
        this.modulo = modulo;
        this.entidadeId = entidadeId;
        this.operacao = operacao;
        this.usuario = usuario;
        this.dataHora = dataHora;
        this.valoresAnteriores = valoresAnteriores;
        this.novosValores = novosValores;
        this.motivo = motivo;
    }

    public UUID getId() {
        return id;
    }

    public ModuloAuditoria getModulo() {
        return modulo;
    }

    public UUID getEntidadeId() {
        return entidadeId;
    }

    public OperacaoAuditoria getOperacao() {
        return operacao;
    }

    public String getUsuario() {
        return usuario;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public Map<String, Object> getValoresAnteriores() {
        return valoresAnteriores;
    }

    public Map<String, Object> getNovosValores() {
        return novosValores;
    }

    public String getMotivo() {
        return motivo;
    }
}
