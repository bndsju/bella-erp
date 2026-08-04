package com.bella.backend.application.cliente.usecase;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.ClientePessoaJuridica;
import com.bella.backend.domain.cliente.model.StatusCliente;
import com.bella.backend.domain.cliente.port.in.AlterarStatusClienteUseCase;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.cliente.port.in.CadastrarClienteUseCase;
import com.bella.backend.domain.cliente.port.in.ComandoCliente;
import com.bella.backend.domain.cliente.port.in.ComandoClientePessoaFisica;
import com.bella.backend.domain.cliente.port.in.ComandoClientePessoaJuridica;
import com.bella.backend.domain.cliente.port.in.EditarClienteUseCase;
import com.bella.backend.domain.cliente.port.in.ListarClientesUseCase;
import com.bella.backend.domain.cliente.port.out.ClienteRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClienteService implements
        CadastrarClienteUseCase,
        EditarClienteUseCase,
        BuscarClientePorIdUseCase,
        ListarClientesUseCase,
        AlterarStatusClienteUseCase {

    private final ClienteRepositoryPort clienteRepositoryPort;

    public ClienteService(ClienteRepositoryPort clienteRepositoryPort) {
        this.clienteRepositoryPort = clienteRepositoryPort;
    }

    @Override
    public Cliente cadastrar(ComandoCliente comando) {
        Cliente cliente = switch (comando) {
            case ComandoClientePessoaFisica pf -> ClientePessoaFisica.novo(
                    pf.nomeCompleto(), pf.dataNascimento(), pf.cpf(),
                    pf.celular(), pf.email(), pf.telefone(), pf.endereco(), pf.observacoes());
            case ComandoClientePessoaJuridica pj -> ClientePessoaJuridica.novo(
                    pj.razaoSocial(), pj.nomeFantasia(), pj.cnpj(), pj.inscricaoEstadual(), pj.inscricaoMunicipal(),
                    pj.celular(), pj.email(), pj.telefone(), pj.endereco(), pj.observacoes());
        };
        garantirDocumentoUnico(cliente, null);
        return clienteRepositoryPort.salvar(cliente);
    }

    @Override
    public Cliente editar(UUID id, ComandoCliente comando) {
        Cliente existente = buscarPorId(id);

        switch (comando) {
            case ComandoClientePessoaFisica pf -> {
                if (!(existente instanceof ClientePessoaFisica clientePf)) {
                    throw new RegraDeNegocioException("Cliente não é Pessoa Física");
                }
                clientePf.editar(pf.nomeCompleto(), pf.dataNascimento(), pf.celular(), pf.email(),
                        pf.telefone(), pf.endereco(), pf.observacoes());
            }
            case ComandoClientePessoaJuridica pj -> {
                if (!(existente instanceof ClientePessoaJuridica clientePj)) {
                    throw new RegraDeNegocioException("Cliente não é Pessoa Jurídica");
                }
                clientePj.editar(pj.razaoSocial(), pj.nomeFantasia(), pj.inscricaoEstadual(), pj.inscricaoMunicipal(),
                        pj.celular(), pj.email(), pj.telefone(), pj.endereco(), pj.observacoes());
            }
        }

        return clienteRepositoryPort.salvar(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorId(UUID id) {
        return clienteRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listar(FiltroListagem filtro) {
        return clienteRepositoryPort.listar(filtro);
    }

    @Override
    public Cliente alterarStatus(UUID id, StatusCliente novoStatus) {
        Cliente cliente = buscarPorId(id);
        if (novoStatus == StatusCliente.ATIVO) {
            cliente.ativar();
        } else {
            cliente.inativar();
        }
        return clienteRepositoryPort.salvar(cliente);
    }

    private void garantirDocumentoUnico(Cliente cliente, UUID idExcluir) {
        if (cliente instanceof ClientePessoaFisica pf && clienteRepositoryPort.existeCpf(pf.getCpf(), idExcluir)) {
            throw new RegraDeNegocioException("Já existe um cliente cadastrado com este CPF");
        }
        if (cliente instanceof ClientePessoaJuridica pj && clienteRepositoryPort.existeCnpj(pj.getCnpj(), idExcluir)) {
            throw new RegraDeNegocioException("Já existe um cliente cadastrado com este CNPJ");
        }
    }
}
