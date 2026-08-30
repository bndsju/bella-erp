package com.bella.backend.adapter.out.persistence.cliente;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.ClientePessoaJuridica;
import com.bella.backend.domain.cliente.model.Endereco;
import com.bella.backend.domain.cliente.port.in.ListarClientesUseCase.FiltroListagem;
import com.bella.backend.domain.cliente.port.out.ClienteRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository clienteJpaRepository;
    private final ClientePfJpaRepository clientePfJpaRepository;
    private final ClientePjJpaRepository clientePjJpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository clienteJpaRepository,
                                     ClientePfJpaRepository clientePfJpaRepository,
                                     ClientePjJpaRepository clientePjJpaRepository) {
        this.clienteJpaRepository = clienteJpaRepository;
        this.clientePfJpaRepository = clientePfJpaRepository;
        this.clientePjJpaRepository = clientePjJpaRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        clienteJpaRepository.save(paraEntidadeBase(cliente));

        switch (cliente) {
            case ClientePessoaFisica pf -> clientePfJpaRepository.save(new ClientePfJpaEntity(
                    pf.getId(), pf.getNomeCompleto(), pf.getDataNascimento(), pf.getCpf()));
            case ClientePessoaJuridica pj -> clientePjJpaRepository.save(new ClientePjJpaEntity(
                    pj.getId(), pj.getRazaoSocial(), pj.getNomeFantasia(), pj.getCnpj(),
                    pj.getInscricaoEstadual(), pj.getInscricaoMunicipal()));
        }

        return cliente;
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return clienteJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Cliente> listar(FiltroListagem filtro) {
        String termo = (filtro.termoBusca() == null || filtro.termoBusca().isBlank()) ? null : filtro.termoBusca();
        String status = filtro.status() == null ? null : filtro.status().name();

        return clienteJpaRepository.buscarIdsPorFiltro(termo, status).stream()
                .map(id -> buscarPorId(id).orElseThrow())
                .toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return clienteJpaRepository.existsById(id);
    }

    @Override
    public boolean existeCpf(String cpf, UUID idExcluir) {
        return idExcluir == null
                ? clientePfJpaRepository.existsByCpf(cpf)
                : clientePfJpaRepository.existsByCpfAndClienteIdNot(cpf, idExcluir);
    }

    @Override
    public boolean existeCnpj(String cnpj, UUID idExcluir) {
        return idExcluir == null
                ? clientePjJpaRepository.existsByCnpj(cnpj)
                : clientePjJpaRepository.existsByCnpjAndClienteIdNot(cnpj, idExcluir);
    }

    private ClienteJpaEntity paraEntidadeBase(Cliente cliente) {
        return new ClienteJpaEntity(
                cliente.getId(),
                cliente.getTipoPessoa(),
                cliente.getTelefone(),
                cliente.getCelular(),
                cliente.getEmail(),
                paraEnderecoEmbeddable(cliente.getEndereco()),
                cliente.getObservacoes(),
                cliente.getStatus(),
                cliente.getCriadoEm(),
                cliente.getAtualizadoEm()
        );
    }

    private Cliente paraDominio(ClienteJpaEntity entidade) {
        Endereco endereco = paraEnderecoDominio(entidade.getEndereco());

        return switch (entidade.getTipoPessoa()) {
            case PF -> {
                ClientePfJpaEntity pf = clientePfJpaRepository.findById(entidade.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Dados de Pessoa Física ausentes para o cliente " + entidade.getId()));
                yield ClientePessoaFisica.existente(entidade.getId(), pf.getNomeCompleto(), pf.getDataNascimento(),
                        pf.getCpf(), entidade.getCelular(), entidade.getEmail(), entidade.getTelefone(), endereco,
                        entidade.getObservacoes(), entidade.getStatus(), entidade.getCriadoEm(),
                        entidade.getAtualizadoEm());
            }
            case PJ -> {
                ClientePjJpaEntity pj = clientePjJpaRepository.findById(entidade.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Dados de Pessoa Jurídica ausentes para o cliente " + entidade.getId()));
                yield ClientePessoaJuridica.existente(entidade.getId(), pj.getRazaoSocial(), pj.getNomeFantasia(),
                        pj.getCnpj(), pj.getInscricaoEstadual(), pj.getInscricaoMunicipal(), entidade.getCelular(),
                        entidade.getEmail(), entidade.getTelefone(), endereco, entidade.getObservacoes(),
                        entidade.getStatus(), entidade.getCriadoEm(), entidade.getAtualizadoEm());
            }
        };
    }

    private EnderecoEmbeddable paraEnderecoEmbeddable(Endereco endereco) {
        return new EnderecoEmbeddable(endereco.cep(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.bairro(), endereco.cidade(), endereco.uf());
    }

    private Endereco paraEnderecoDominio(EnderecoEmbeddable embeddable) {
        return new Endereco(embeddable.getCep(), embeddable.getLogradouro(), embeddable.getNumero(),
                embeddable.getComplemento(), embeddable.getBairro(), embeddable.getCidade(), embeddable.getUf());
    }
}
