export type TipoMovimentacaoCaixa = "ENTRADA" | "SAIDA";

export type SituacaoMovimentacaoCaixa = "PREVISTO" | "REALIZADO";

export type OrigemMovimentacaoCaixa = "CONTA_RECEBER" | "CONTA_PAGAR" | "LANCAMENTO_MANUAL";

export interface LancamentoManualCaixaRequest {
  tipo: TipoMovimentacaoCaixa;
  descricao: string;
  categoriaId: string;
  valor: number;
  situacao: SituacaoMovimentacaoCaixa;
  dataPrevista: string;
  dataEfetiva?: string;
  observacoes?: string;
}

export interface MovimentacaoCaixaResponse {
  id: string;
  tipo: TipoMovimentacaoCaixa;
  descricao: string;
  categoriaId: string | null;
  categoriaNome: string | null;
  valor: number;
  situacao: SituacaoMovimentacaoCaixa;
  dataPrevista: string;
  dataEfetiva: string | null;
  origem: OrigemMovimentacaoCaixa;
  origemReferenciaId: string | null;
  observacoes: string | null;
  cancelada: boolean;
  motivoCancelamento: string | null;
  canceladaEm: string | null;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ResumoCaixaResponse {
  entradasPrevistas: number;
  saidasPrevistas: number;
  entradasRealizadas: number;
  saidasRealizadas: number;
  saldoRealizado: number;
  saldoProjetado: number;
}

export interface ResumoCategoriaCaixaResponse {
  categoriaId: string | null;
  categoriaNome: string | null;
  resumo: ResumoCaixaResponse;
}

export interface ListarMovimentacoesCaixaParams {
  tipo?: TipoMovimentacaoCaixa;
  categoriaId?: string;
  origem?: OrigemMovimentacaoCaixa;
  situacao?: SituacaoMovimentacaoCaixa;
  dataInicio?: string;
  dataFim?: string;
  incluirCanceladas?: boolean;
}
