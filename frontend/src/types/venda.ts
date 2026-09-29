export type StatusVenda = "CONCLUIDA" | "CANCELADA";

export type FormaPagamento =
  | "DINHEIRO"
  | "CARTAO_CREDITO"
  | "CARTAO_DEBITO"
  | "PIX"
  | "BOLETO"
  | "TRANSFERENCIA";

export interface ConcluirVendaRequest {
  pedidoId: string;
  formaPagamento: FormaPagamento;
}

export interface VendaItemResponse {
  id: string;
  produtoId: string;
  produtoNome: string;
  produtoCodigoInterno: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
}

export interface VendaResponse {
  id: string;
  pedidoId: string;
  clienteId: string;
  clienteNome: string;
  itens: VendaItemResponse[];
  percentualDesconto: number;
  valorFrete: number;
  subtotal: number;
  valorDesconto: number;
  valorTotal: number;
  formaPagamento: FormaPagamento;
  condicaoPagamento: string;
  status: StatusVenda;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListarVendasParams {
  clienteId?: string;
  status?: StatusVenda;
}
