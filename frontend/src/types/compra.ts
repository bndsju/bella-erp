export type StatusCompra = "RASCUNHO" | "CONFIRMADA" | "RECEBIDA" | "CANCELADA";

export interface CompraItemRequest {
  produtoId: string;
  quantidade: number;
  custoUnitario: number;
  desconto?: number;
}

export interface CompraRequest {
  fornecedorId: string;
  previsaoRecebimento?: string;
  itens: CompraItemRequest[];
  descontoGeral?: number;
  frete?: number;
  outrasDespesas?: number;
  condicaoPagamento: string;
  observacoes?: string;
}

export interface CompraItemResponse {
  id: string;
  produtoId: string;
  produtoNome: string;
  produtoCodigoInterno: string;
  quantidade: number;
  custoUnitario: number;
  desconto: number;
  valorTotal: number;
}

export interface CompraResponse {
  id: string;
  fornecedorId: string;
  fornecedorNome: string;
  dataCompra: string;
  previsaoRecebimento: string | null;
  dataRecebimento: string | null;
  itens: CompraItemResponse[];
  valorProdutos: number;
  descontoGeral: number;
  descontoTotal: number;
  frete: number;
  outrasDespesas: number;
  valorTotal: number;
  condicaoPagamento: string;
  observacoes: string | null;
  status: StatusCompra;
  motivoCancelamento: string | null;
  confirmadaEm: string | null;
  canceladaEm: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListarComprasParams {
  fornecedorId?: string;
  status?: StatusCompra;
  dataInicio?: string;
  dataFim?: string;
}
