package com.bella.backend.application.auditoria;

import com.bella.backend.domain.auditoria.model.ModuloAuditoria;
import com.bella.backend.domain.cliente.port.in.BuscarClientePorIdUseCase;
import com.bella.backend.domain.compra.port.in.BuscarCompraPorIdUseCase;
import com.bella.backend.domain.contapagar.port.in.BuscarContaPagarPorIdUseCase;
import com.bella.backend.domain.contareceber.port.in.BuscarContaReceberPorIdUseCase;
import com.bella.backend.domain.orcamento.port.in.BuscarOrcamentoPorIdUseCase;
import com.bella.backend.domain.pedido.port.in.BuscarPedidoPorIdUseCase;
import com.bella.backend.domain.produto.port.in.BuscarProdutoPorIdUseCase;
import com.bella.backend.domain.venda.port.in.BuscarVendaPorIdUseCase;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Único lugar que sabe como ler o estado de um registro de cada módulo, para registrar o valor
 * anterior antes de uma alteração. Usa as consultas já existentes dos módulos. Estoque não aparece
 * aqui porque suas movimentações são apenas criadas, nunca alteradas.
 */
@Component
public class CarregadoresAuditoria {

    private final Map<ModuloAuditoria, Function<UUID, Object>> carregadores = new EnumMap<>(ModuloAuditoria.class);

    public CarregadoresAuditoria(BuscarClientePorIdUseCase clientes,
                                 BuscarProdutoPorIdUseCase produtos,
                                 BuscarOrcamentoPorIdUseCase orcamentos,
                                 BuscarPedidoPorIdUseCase pedidos,
                                 BuscarVendaPorIdUseCase vendas,
                                 BuscarCompraPorIdUseCase compras,
                                 BuscarContaReceberPorIdUseCase contasReceber,
                                 BuscarContaPagarPorIdUseCase contasPagar) {
        carregadores.put(ModuloAuditoria.CLIENTE, clientes::buscarPorId);
        carregadores.put(ModuloAuditoria.PRODUTO, produtos::buscarPorId);
        carregadores.put(ModuloAuditoria.ORCAMENTO, orcamentos::buscarPorId);
        carregadores.put(ModuloAuditoria.PEDIDO, pedidos::buscarPorId);
        carregadores.put(ModuloAuditoria.VENDA, vendas::buscarPorId);
        carregadores.put(ModuloAuditoria.COMPRA, compras::buscarPorId);
        carregadores.put(ModuloAuditoria.CONTA_RECEBER, contasReceber::buscarPorId);
        carregadores.put(ModuloAuditoria.CONTA_PAGAR, contasPagar::buscarPorId);
    }

    public Object carregar(ModuloAuditoria modulo, UUID id) {
        Function<UUID, Object> carregador = carregadores.get(modulo);
        if (carregador == null) {
            throw new IllegalStateException("Módulo sem leitura de estado anterior: " + modulo);
        }
        return carregador.apply(id);
    }
}
