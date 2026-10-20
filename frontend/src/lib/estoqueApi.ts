import { api } from "./api";
import type {
  ListarMovimentacoesParams,
  MovimentacaoEstoqueResponse,
  MovimentacaoManualRequest,
  SaldoEstoqueResponse,
  TipoMovimentacaoManual,
} from "@/types/estoque";

const ROTA_POR_TIPO: Record<TipoMovimentacaoManual, string> = {
  ENTRADA: "/estoque/entradas",
  SAIDA: "/estoque/saidas",
  AJUSTE_ENTRADA: "/estoque/ajustes/entrada",
  AJUSTE_SAIDA: "/estoque/ajustes/saida",
};

function queryString(params: ListarMovimentacoesParams): string {
  const query = new URLSearchParams();
  if (params.produtoId) query.set("produtoId", params.produtoId);
  if (params.tipo) query.set("tipo", params.tipo);
  if (params.dataInicio) query.set("dataInicio", params.dataInicio);
  if (params.dataFim) query.set("dataFim", params.dataFim);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const estoqueApi = {
  listarSaldos: () => api.get<SaldoEstoqueResponse[]>("/estoque"),

  listarAbaixoDoMinimo: () => api.get<SaldoEstoqueResponse[]>("/estoque/abaixo-do-minimo"),

  buscarSaldoPorProduto: (produtoId: string) =>
    api.get<SaldoEstoqueResponse>(`/estoque/produtos/${produtoId}`),

  listarMovimentacoes: (params: ListarMovimentacoesParams = {}) =>
    api.get<MovimentacaoEstoqueResponse[]>(`/estoque/movimentacoes${queryString(params)}`),

  registrarMovimentacao: (tipo: TipoMovimentacaoManual, dados: MovimentacaoManualRequest) =>
    api.post<MovimentacaoEstoqueResponse>(ROTA_POR_TIPO[tipo], dados),
};
