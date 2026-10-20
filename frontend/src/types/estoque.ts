export type TipoMovimentacaoEstoque =
  | "ENTRADA"
  | "SAIDA"
  | "AJUSTE_ENTRADA"
  | "AJUSTE_SAIDA"
  | "RESERVA"
  | "LIBERACAO_RESERVA";

export type TipoMovimentacaoManual = "ENTRADA" | "SAIDA" | "AJUSTE_ENTRADA" | "AJUSTE_SAIDA";

export type OrigemMovimentacaoEstoque = "MANUAL" | "PEDIDO" | "COMPRA";

export interface SaldoEstoqueResponse {
  produtoId: string;
  produtoNome: string;
  produtoCodigoInterno: string;
  estoqueFisico: number;
  quantidadeReservada: number;
  quantidadeDisponivel: number;
  estoqueMinimo: number;
}

export interface MovimentacaoEstoqueResponse {
  id: string;
  produtoId: string;
  produtoNome: string;
  produtoCodigoInterno: string;
  tipo: TipoMovimentacaoEstoque;
  quantidade: number;
  motivo: string;
  observacao: string | null;
  origem: OrigemMovimentacaoEstoque;
  origemOperacaoId: string | null;
  usuarioResponsavel: string | null;
  dataHora: string;
}

export interface MovimentacaoManualRequest {
  produtoId: string;
  quantidade: number;
  motivo: string;
  observacao?: string;
}

export interface ListarMovimentacoesParams {
  produtoId?: string;
  tipo?: TipoMovimentacaoEstoque;
  dataInicio?: string;
  dataFim?: string;
}
