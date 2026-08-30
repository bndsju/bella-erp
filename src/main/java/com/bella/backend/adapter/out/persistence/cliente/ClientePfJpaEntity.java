package com.bella.backend.adapter.out.persistence.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "clientes_pf", schema = "bella")
public class ClientePfJpaEntity {

    @Id
    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 11)
    private String cpf;

    protected ClientePfJpaEntity() {
    }

    public ClientePfJpaEntity(UUID clienteId, String nomeCompleto, LocalDate dataNascimento, String cpf) {
        this.clienteId = clienteId;
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }
}
