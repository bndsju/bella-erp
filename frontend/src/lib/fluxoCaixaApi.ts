import { api } from "./api";
import type {
  LancamentoManualCaixaRequest,
  ListarMovimentacoesCaixaParams,
  MovimentacaoCaixaResponse,
  ResumoCaixaResponse,
  ResumoCategoriaCaixaResponse,
} from "@/types/fluxoCaixa";

function queryString(params: Record<string, string | boolean | undefined>): string {
  const query = new URLSearchParams();
  for (const [chave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== "") query.set(chave, String(valor));
  }
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const fluxoCaixaApi = {
  listar: (params: ListarMovimentacoesCaixaParams = {}) =>
    api.get<MovimentacaoCaixaResponse[]>(`/fluxo-caixa/movimentacoes${queryString({ ...params })}`),

  lancar: (dados: LancamentoManualCaixaRequest) =>
    api.post<MovimentacaoCaixaResponse>("/fluxo-caixa/movimentacoes", dados),

  editar: (id: string, dados: LancamentoManualCaixaRequest) =>
    api.put<MovimentacaoCaixaResponse>(`/fluxo-caixa/movimentacoes/${id}`, dados),

  realizar: (id: string, dataEfetiva?: string) =>
    api.patch<MovimentacaoCaixaResponse>(`/fluxo-caixa/movimentacoes/${id}/realizar`, { dataEfetiva }),

  cancelar: (id: string, motivo: string) =>
    api.patch<MovimentacaoCaixaResponse>(`/fluxo-caixa/movimentacoes/${id}/cancelar`, { motivo }),

  resumo: (inicio?: string, fim?: string) =>
    api.get<ResumoCaixaResponse>(`/fluxo-caixa/resumo${queryString({ inicio, fim })}`),

  saldoAcumulado: (ate?: string) =>
    api.get<ResumoCaixaResponse>(`/fluxo-caixa/saldo-acumulado${queryString({ ate })}`),

  resumoPorCategoria: (inicio?: string, fim?: string) =>
    api.get<ResumoCategoriaCaixaResponse[]>(`/fluxo-caixa/resumo-por-categoria${queryString({ inicio, fim })}`),
};
