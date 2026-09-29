export type StatusOrcamento = "RASCUNHO" | "ENVIADO" | "APROVADO" | "RECUSADO" | "EXPIRADO" | "CANCELADO";

export interface OrcamentoItemRequest {
  produtoId: string;
  quantidade: number;
  valorUnitario: number;
}

export interface OrcamentoRequest {
  clienteId: string;
  itens: OrcamentoItemRequest[];
  percentualDesconto: number;
  valorFrete: number;
  prazoValidade: string;
  condicaoPagamento: string;
  observacoes?: string;
}

export interface OrcamentoItemResponse {
  id: string;
  produtoId: string;
  produtoNome: string;
  produtoCodigoInterno: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
}

export interface OrcamentoResponse {
  id: string;
  clienteId: string;
  clienteNome: string;
  itens: OrcamentoItemResponse[];
  percentualDesconto: number;
  valorFrete: number;
  subtotal: number;
  valorDesconto: number;
  valorTotal: number;
  prazoValidade: string;
  condicaoPagamento: string;
  observacoes: string | null;
  status: StatusOrcamento;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListarOrcamentosParams {
  clienteId?: string;
  status?: StatusOrcamento;
}
