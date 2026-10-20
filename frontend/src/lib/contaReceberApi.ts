import { api } from "./api";
import type {
  ContaReceberRequest,
  ContaReceberResponse,
  DadosBasicosContaReceberRequest,
  ListarContasReceberParams,
  RecebimentoContaReceberRequest,
} from "@/types/contaReceber";

function queryString(params: ListarContasReceberParams): string {
  const query = new URLSearchParams();
  if (params.clienteId) query.set("clienteId", params.clienteId);
  if (params.status) query.set("status", params.status);
  if (params.situacao) query.set("situacao", params.situacao);
  if (params.vencimentoInicio) query.set("vencimentoInicio", params.vencimentoInicio);
  if (params.vencimentoFim) query.set("vencimentoFim", params.vencimentoFim);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const contaReceberApi = {
  listar: (params: ListarContasReceberParams = {}) =>
    api.get<ContaReceberResponse[]>(`/contas-receber${queryString(params)}`),

  buscarPorId: (id: string) => api.get<ContaReceberResponse>(`/contas-receber/${id}`),

  cadastrar: (dados: ContaReceberRequest) => api.post<ContaReceberResponse>("/contas-receber", dados),

  editar: (id: string, dados: ContaReceberRequest) =>
    api.put<ContaReceberResponse>(`/contas-receber/${id}`, dados),

  editarDadosBasicos: (id: string, dados: DadosBasicosContaReceberRequest) =>
    api.patch<ContaReceberResponse>(`/contas-receber/${id}/dados-basicos`, dados),

  registrarRecebimento: (id: string, dados: RecebimentoContaReceberRequest) =>
    api.post<ContaReceberResponse>(`/contas-receber/${id}/recebimentos`, dados),

  cancelar: (id: string, motivo: string) =>
    api.patch<ContaReceberResponse>(`/contas-receber/${id}/cancelar`, { motivo }),
};
