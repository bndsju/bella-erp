export type StatusPedido =
  | "CRIADO"
  | "CONFIRMADO"
  | "EM_SEPARACAO"
  | "PRONTO_PARA_ENTREGA"
  | "ENTREGUE"
  | "CANCELADO";

export interface PedidoItemRequest {
  produtoId: string;
  quantidade: number;
  valorUnitario: number;
}

export interface PedidoRequest {
  clienteId: string;
  transportadoraId?: string;
  itens: PedidoItemRequest[];
  percentualDesconto: number;
  valorFrete: number;
  condicaoPagamento: string;
}

export interface CriarPedidoDeOrcamentoRequest {
  orcamentoId: string;
  transportadoraId?: string;
}

export interface PedidoItemResponse {
  id: string;
  produtoId: string;
  produtoNome: string;
  produtoCodigoInterno: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
}

export interface PedidoResponse {
  id: string;
  clienteId: string;
  clienteNome: string;
  orcamentoOrigemId: string | null;
  transportadoraId: string | null;
  transportadoraNome: string | null;
  itens: PedidoItemResponse[];
  percentualDesconto: number;
  valorFrete: number;
  subtotal: number;
  valorDesconto: number;
  valorTotal: number;
  condicaoPagamento: string;
  status: StatusPedido;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListarPedidosParams {
  clienteId?: string;
  status?: StatusPedido;
}
