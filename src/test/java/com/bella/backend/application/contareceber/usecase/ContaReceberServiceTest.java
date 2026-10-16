package com.bella.backend.application.contareceber.usecase;

import com.bella.backend.domain.cliente.model.Cliente;
import com.bella.backend.domain.cliente.model.ClientePessoaFisica;
import com.bella.backend.domain.cliente.model.Endereco;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.contareceber.model.ContaReceber;
import com.bella.backend.domain.contareceber.model.DadosParcela;
import com.bella.backend.domain.contareceber.model.OrigemContaReceber;
import com.bella.backend.domain.contareceber.model.SituacaoContaReceber;
import com.bella.backend.domain.contareceber.model.StatusContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoParcelaContaReceber;
import com.bella.backend.domain.contareceber.port.in.ComandoRecebimentoContaReceber;
import com.bella.backend.domain.contareceber.port.in.ListarContasReceberUseCase.FiltroListagem;
import com.bella.backend.domain.contareceber.port.out.ContaReceberRepositoryPort;
import com.bella.backend.domain.shared.EntidadeNaoEncontradaException;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaReceberServiceTest {

    @Mock
    private ContaReceberRepositoryPort contaReceberRepositoryPort;

    @Mock
    private BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    private ContaReceberService contaReceberService;

    private static final UUID CLIENTE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        contaReceberService = new ContaReceberService(contaReceberRepositoryPort, buscarClientePorIdUseCase);
    }

    private Cliente clienteAtivo() {
        Endereco endereco = new Endereco("01310-100", "Av. Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        return ClientePessoaFisica.novo("Maria Silva", LocalDate.of(1990, 5, 10), "111.444.777-35",
                "11999998888", "maria@example.com", null, endereco, null);
    }

    private ComandoContaReceber comandoUnico(UUID clienteId) {
        return new ComandoContaReceber("Mensalidade", clienteId, new BigDecimal("1500.00"),
                LocalDate.now().plusDays(10), null, null, OrigemContaReceber.MANUAL, null);
    }

    private ContaReceber conta(LocalDate vencimento) {
        return ContaReceber.nova("Conta", null, new BigDecimal("100.00"), vencimento, null, null,
                OrigemContaReceber.MANUAL, null);
    }

    @Test
    void cadastraContaManualSemCliente() {
        when(contaReceberRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ContaReceber conta = contaReceberService.cadastrar(comandoUnico(null));

        assertThat(conta.getClienteId()).isNull();
        assertThat(conta.getOrigem()).isEqualTo(OrigemContaReceber.MANUAL);
        assertThat(conta.getParcelas()).hasSize(1);
        verify(buscarClientePorIdUseCase, never()).buscarPorId(any());
    }

    @Test
    void cadastraContaParceladaComClienteAtivo() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(clienteAtivo());
        when(contaReceberRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ComandoContaReceber comando = new ComandoContaReceber("Serviço", CLIENTE_ID, new BigDecimal("300.00"), null,
                List.of(new ComandoParcelaContaReceber(new BigDecimal("150.00"), LocalDate.now().plusDays(30)),
                        new ComandoParcelaContaReceber(new BigDecimal("150.00"), LocalDate.now().plusDays(60))),
                null, OrigemContaReceber.MANUAL, null);

        ContaReceber conta = contaReceberService.cadastrar(comando);

        assertThat(conta.getParcelas()).hasSize(2);
        assertThat(conta.getClienteId()).isEqualTo(CLIENTE_ID);
    }

    @Test
    void rejeitaClienteInativo() {
        Cliente inativo = clienteAtivo();
        inativo.inativar();
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(inativo);

        assertThrows(RegraDeNegocioException.class, () -> contaReceberService.cadastrar(comandoUnico(CLIENTE_ID)));
        verify(contaReceberRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaClienteInexistente() {
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID))
                .thenThrow(new EntidadeNaoEncontradaException("Cliente", CLIENTE_ID));

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> contaReceberService.cadastrar(comandoUnico(CLIENTE_ID)));
        verify(contaReceberRepositoryPort, never()).salvar(any());
    }

    @Test
    void contaDeVendaExigeClienteEReferencia() {
        ComandoContaReceber semCliente = new ComandoContaReceber("Venda", null, BigDecimal.TEN, LocalDate.now(),
                null, null, OrigemContaReceber.VENDA, UUID.randomUUID());

        assertThrows(RegraDeNegocioException.class, () -> contaReceberService.cadastrar(semCliente));
        verify(contaReceberRepositoryPort, never()).salvar(any());
    }

    @Test
    void rejeitaSegundaContaParaMesmaVenda() {
        UUID vendaId = UUID.randomUUID();
        when(buscarClientePorIdUseCase.buscarPorId(CLIENTE_ID)).thenReturn(clienteAtivo());
        when(contaReceberRepositoryPort.existePorOrigemEReferencia(OrigemContaReceber.VENDA, vendaId))
                .thenReturn(true);

        ComandoContaReceber comando = new ComandoContaReceber("Venda", CLIENTE_ID, BigDecimal.TEN, LocalDate.now(),
                null, null, OrigemContaReceber.VENDA, vendaId);

        assertThrows(RegraDeNegocioException.class, () -> contaReceberService.cadastrar(comando));
        verify(contaReceberRepositoryPort, never()).salvar(any());
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(contaReceberRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> contaReceberService.buscarPorId(id));
    }

    @Test
    void registraRecebimentoUsandoDataDeHojeQuandoNaoInformada() {
        ContaReceber conta = conta(LocalDate.now().plusDays(5));
        when(contaReceberRepositoryPort.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));
        when(contaReceberRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ContaReceber atualizada = contaReceberService.registrarRecebimento(conta.getId(),
                new ComandoRecebimentoContaReceber(conta.getParcelas().get(0).getId(), new BigDecimal("40.00"), null,
                        null, null, null, null));

        assertThat(atualizada.getStatus()).isEqualTo(StatusContaReceber.PARCIALMENTE_RECEBIDA);
        assertThat(atualizada.getParcelas().get(0).getRecebimentos().get(0).getDataRecebimento())
                .isEqualTo(LocalDate.now());
    }

    @Test
    void recebimentoAcimaDoSaldoNaoEhSalvo() {
        ContaReceber conta = conta(LocalDate.now().plusDays(5));
        when(contaReceberRepositoryPort.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));

        assertThrows(RegraDeNegocioException.class, () -> contaReceberService.registrarRecebimento(conta.getId(),
                new ComandoRecebimentoContaReceber(conta.getParcelas().get(0).getId(), new BigDecimal("100.01"),
                        null, null, null, null, null)));
        verify(contaReceberRepositoryPort, never()).salvar(any());
    }

    @Test
    void cancelaContaSemRecebimentos() {
        ContaReceber conta = conta(LocalDate.now().plusDays(5));
        when(contaReceberRepositoryPort.buscarPorId(conta.getId())).thenReturn(Optional.of(conta));
        when(contaReceberRepositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(contaReceberService.cancelar(conta.getId(), "Duplicada").getStatus())
                .isEqualTo(StatusContaReceber.CANCELADA);
    }

    @Test
    void listaApenasVencidasEmAbertoERecebidasConformeSituacao() {
        ContaReceber atrasada = conta(LocalDate.now().minusDays(5));
        ContaReceber noPrazo = conta(LocalDate.now().plusDays(5));
        ContaReceber recebida = conta(LocalDate.now().minusDays(1));
        recebida.registrarRecebimento(recebida.getParcelas().get(0).getId(), new BigDecimal("100.00"),
                LocalDate.now(), null, null, null, null);
        when(contaReceberRepositoryPort.listar(null, null)).thenReturn(List.of(atrasada, noPrazo, recebida));

        assertThat(contaReceberService.listar(new FiltroListagem(null, null, SituacaoContaReceber.VENCIDAS,
                null, null))).containsExactly(atrasada);
        assertThat(contaReceberService.listar(new FiltroListagem(null, null, SituacaoContaReceber.EM_ABERTO,
                null, null))).containsExactly(atrasada, noPrazo);
        assertThat(contaReceberService.listar(new FiltroListagem(null, null, SituacaoContaReceber.RECEBIDAS,
                null, null))).containsExactly(recebida);
    }

    @Test
    void listaFiltrandoPorPeriodoDeVencimentoDasParcelas() {
        ContaReceber cedo = conta(LocalDate.now().plusDays(2));
        ContaReceber tarde = conta(LocalDate.now().plusDays(40));
        ContaReceber parcelada = ContaReceber.nova("Parcelada", null, new BigDecimal("200.00"), null,
                List.of(new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(1)),
                        new DadosParcela(new BigDecimal("100.00"), LocalDate.now().plusDays(35))),
                null, OrigemContaReceber.MANUAL, null);
        when(contaReceberRepositoryPort.listar(null, null)).thenReturn(List.of(cedo, tarde, parcelada));

        List<ContaReceber> resultado = contaReceberService.listar(new FiltroListagem(null, null, null,
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(45)));

        assertThat(resultado).containsExactly(tarde, parcelada);
    }

    @Test
    void listaRepassandoFiltrosSimplesAoRepositorio() {
        when(contaReceberRepositoryPort.listar(CLIENTE_ID, StatusContaReceber.PENDENTE)).thenReturn(List.of());

        assertThat(contaReceberService.listar(new FiltroListagem(CLIENTE_ID, StatusContaReceber.PENDENTE, null,
                null, null))).isEmpty();
    }
}
