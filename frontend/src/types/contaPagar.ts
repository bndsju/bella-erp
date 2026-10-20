export type StatusContaPagar = "PENDENTE" | "PARCIALMENTE_PAGA" | "PAGA" | "CANCELADA";

export type StatusParcela = "PENDENTE" | "PARCIALMENTE_PAGA" | "PAGA" | "CANCELADA";

export type OrigemContaPagar = "MANUAL" | "COMPRA";

export type SituacaoContaPagar = "EM_ABERTO" | "PAGAS" | "VENCIDAS";

export interface ParcelaContaPagarRequest {
  valor: number;
  vencimento: string;
}

export interface ContaPagarRequest {
  descricao: string;
  fornecedorId?: string;
  categoriaDespesaId: string;
  valorTotal: number;
  vencimento?: string;
  parcelas?: ParcelaContaPagarRequest[];
  observacoes?: string;
}

export interface DadosBasicosContaPagarRequest {
  descricao: string;
  fornecedorId?: string;
  categoriaDespesaId: string;
  observacoes?: string;
}

export interface PagamentoContaPagarRequest {
  parcelaId: string;
  valorPago: number;
  dataPagamento?: string;
  juros?: number;
  multa?: number;
  desconto?: number;
  observacao?: string;
}

export interface PagamentoContaPagarResponse {
  id: string;
  valorPago: number;
  dataPagamento: string;
  juros: number;
  multa: number;
  desconto: number;
  valorDesembolsado: number;
  observacao: string | null;
  criadoEm: string;
}

export interface ParcelaContaPagarResponse {
  id: string;
  numero: number;
  valor: number;
  vencimento: string;
  status: StatusParcela;
  vencida: boolean;
  valorPago: number;
  saldoPendente: number;
  pagamentos: PagamentoContaPagarResponse[];
}

export interface ContaPagarResponse {
  id: string;
  descricao: string;
  fornecedorId: string | null;
  fornecedorNome: string | null;
  categoriaDespesaId: string;
  categoriaDespesaNome: string;
  valorTotal: number;
  valorPago: number;
  saldoPendente: number;
  dataLancamento: string;
  observacoes: string | null;
  origem: OrigemContaPagar;
  origemReferenciaId: string | null;
  status: StatusContaPagar;
  vencida: boolean;
  parcelas: ParcelaContaPagarResponse[];
  motivoCancelamento: string | null;
  canceladaEm: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListarContasPagarParams {
  fornecedorId?: string;
  categoriaDespesaId?: string;
  status?: StatusContaPagar;
  situacao?: SituacaoContaPagar;
  vencimentoInicio?: string;
  vencimentoFim?: string;
}
