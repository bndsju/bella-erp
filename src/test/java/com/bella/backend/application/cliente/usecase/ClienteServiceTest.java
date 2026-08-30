package com.bella.backend.application.cliente.usecase;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.Endereco;
import com.bella.backend.domain.cliente.model.StatusCliente;
import com.bella.backend.domain.cliente.port.in.ComandoClientePessoaFisica;
import com.bella.backend.domain.cliente.port.in.ComandoClientePessoaJuridica;
import com.bella.backend.domain.cliente.port.in.ListarClientesUseCase.FiltroListagem;
import com.bella.backend.domain.cliente.port.out.ClienteRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepositoryPort clienteRepositoryPort;

    private ClienteService clienteService;

    private static final Endereco ENDERECO =
            new Endereco("01310-100", "Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(clienteRepositoryPort);
    }

    @Test
    void cadastraClientePessoaFisicaQuandoCpfNaoExiste() {
        when(clienteRepositoryPort.existeCpf(eq("11144477735"), eq(null))).thenReturn(false);
        when(clienteRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ComandoClientePessoaFisica comando = new ComandoClientePessoaFisica(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);

        Cliente cliente = clienteService.cadastrar(comando);

        assertThat(cliente).isInstanceOf(ClientePessoaFisica.class);
        verify(clienteRepositoryPort).salvar(any());
    }

    @Test
    void rejeitaCadastroComCpfDuplicado() {
        when(clienteRepositoryPort.existeCpf(eq("11144477735"), eq(null))).thenReturn(true);

        ComandoClientePessoaFisica comando = new ComandoClientePessoaFisica(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);

        assertThrows(RegraDeNegocioException.class, () -> clienteService.cadastrar(comando));
        verify(clienteRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(clienteRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> clienteService.buscarPorId(id));
    }

    @Test
    void editarComTipoDiferenteLancaRegraDeNegocio() {
        UUID id = UUID.randomUUID();
        ClientePessoaFisica existente = ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);
        when(clienteRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));

        ComandoClientePessoaJuridica comandoPj = new ComandoClientePessoaJuridica(
                "Padaria Pão Quente Ltda", "Pão Quente", "11.222.333/0001-81",
                null, null, "11999998888", "contato@paoquente.com", null, ENDERECO, null);

        assertThrows(RegraDeNegocioException.class, () -> clienteService.editar(id, comandoPj));
    }

    @Test
    void alterarStatusInativaCliente() {
        UUID id = UUID.randomUUID();
        ClientePessoaFisica existente = ClientePessoaFisica.novo(
                "Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, ENDERECO, null);
        when(clienteRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(existente));
        when(clienteRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente resultado = clienteService.alterarStatus(id, StatusCliente.INATIVO);

        assertThat(resultado.getStatus()).isEqualTo(StatusCliente.INATIVO);
    }

    @Test
    void listarDelegaParaRepositorio() {
        FiltroListagem filtro = new FiltroListagem("Maria", StatusCliente.ATIVO);
        when(clienteRepositoryPort.listar(filtro)).thenReturn(List.of());

        List<Cliente> resultado = clienteService.listar(filtro);

        assertThat(resultado).isEmpty();
        verify(clienteRepositoryPort).listar(filtro);
    }
}
