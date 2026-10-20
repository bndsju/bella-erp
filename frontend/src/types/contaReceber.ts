export type StatusContaReceber = "PENDENTE" | "PARCIALMENTE_RECEBIDA" | "RECEBIDA" | "CANCELADA";

export type StatusParcela = "PENDENTE" | "PARCIALMENTE_RECEBIDA" | "RECEBIDA" | "CANCELADA";

export type OrigemContaReceber = "MANUAL" | "VENDA";

export type SituacaoContaReceber = "EM_ABERTO" | "RECEBIDAS" | "VENCIDAS";

export interface ParcelaContaReceberRequest {
  valor: number;
  vencimento: string;
}

export interface ContaReceberRequest {
  descricao: string;
  clienteId?: string;
  valorTotal: number;
  vencimento?: string;
  parcelas?: ParcelaContaReceberRequest[];
  observacoes?: string;
}

export interface DadosBasicosContaReceberRequest {
  descricao: string;
  clienteId?: string;
  observacoes?: string;
}

export interface RecebimentoContaReceberRequest {
  parcelaId: string;
  valorRecebido: number;
  dataRecebimento?: string;
  juros?: number;
  multa?: number;
  desconto?: number;
  observacao?: string;
}

export interface RecebimentoContaReceberResponse {
  id: string;
  valorRecebido: number;
  dataRecebimento: string;
  juros: number;
  multa: number;
  desconto: number;
  valorTotalRecebido: number;
  observacao: string | null;
  criadoEm: string;
}

export interface ParcelaContaReceberResponse {
  id: string;
  numero: number;
  valor: number;
  vencimento: string;
  status: StatusParcela;
  vencida: boolean;
  valorRecebido: number;
  saldoPendente: number;
  recebimentos: RecebimentoContaReceberResponse[];
}

export interface ContaReceberResponse {
  id: string;
  descricao: string;
  clienteId: string | null;
  clienteNome: string | null;
  valorTotal: number;
  valorRecebido: number;
  saldoPendente: number;
  dataLancamento: string;
  observacoes: string | null;
  origem: OrigemContaReceber;
  origemReferenciaId: string | null;
  status: StatusContaReceber;
  vencida: boolean;
  parcelas: ParcelaContaReceberResponse[];
  motivoCancelamento: string | null;
  canceladaEm: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ListarContasReceberParams {
  clienteId?: string;
  status?: StatusContaReceber;
  situacao?: SituacaoContaReceber;
  vencimentoInicio?: string;
  vencimentoFim?: string;
}
