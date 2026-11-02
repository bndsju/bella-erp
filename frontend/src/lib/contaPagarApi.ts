import { api } from "./api";
import type {
  ContaPagarRequest,
  ContaPagarResponse,
  DadosBasicosContaPagarRequest,
  ListarContasPagarParams,
  PagamentoContaPagarRequest,
} from "@/types/contaPagar";

function queryString(params: ListarContasPagarParams): string {
  const query = new URLSearchParams();
  if (params.fornecedorId) query.set("fornecedorId", params.fornecedorId);
  if (params.categoriaDespesaId) query.set("categoriaDespesaId", params.categoriaDespesaId);
  if (params.status) query.set("status", params.status);
  if (params.situacao) query.set("situacao", params.situacao);
  if (params.vencimentoInicio) query.set("vencimentoInicio", params.vencimentoInicio);
  if (params.vencimentoFim) query.set("vencimentoFim", params.vencimentoFim);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const contaPagarApi = {
  listar: (params: ListarContasPagarParams = {}) =>
    api.get<ContaPagarResponse[]>(`/contas-pagar${queryString(params)}`),

  buscarPorId: (id: string) => api.get<ContaPagarResponse>(`/contas-pagar/${id}`),

  cadastrar: (dados: ContaPagarRequest) => api.post<ContaPagarResponse>("/contas-pagar", dados),

  editar: (id: string, dados: ContaPagarRequest) =>
    api.put<ContaPagarResponse>(`/contas-pagar/${id}`, dados),

  editarDadosBasicos: (id: string, dados: DadosBasicosContaPagarRequest) =>
    api.patch<ContaPagarResponse>(`/contas-pagar/${id}/dados-basicos`, dados),

  registrarPagamento: (id: string, dados: PagamentoContaPagarRequest) =>
    api.post<ContaPagarResponse>(`/contas-pagar/${id}/pagamentos`, dados),

  cancelar: (id: string, motivo: string) =>
    api.patch<ContaPagarResponse>(`/contas-pagar/${id}/cancelar`, { motivo }),
};
