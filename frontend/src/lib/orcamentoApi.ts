import { api } from "./api";
import type {
  ListarOrcamentosParams,
  OrcamentoRequest,
  OrcamentoResponse,
} from "@/types/orcamento";

function queryString(params: ListarOrcamentosParams): string {
  const query = new URLSearchParams();
  if (params.clienteId) query.set("clienteId", params.clienteId);
  if (params.status) query.set("status", params.status);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const orcamentoApi = {
  listar: (params: ListarOrcamentosParams = {}) =>
    api.get<OrcamentoResponse[]>(`/orcamentos${queryString(params)}`),

  buscarPorId: (id: string) => api.get<OrcamentoResponse>(`/orcamentos/${id}`),

  cadastrar: (dados: OrcamentoRequest) => api.post<OrcamentoResponse>("/orcamentos", dados),

  editar: (id: string, dados: OrcamentoRequest) =>
    api.put<OrcamentoResponse>(`/orcamentos/${id}`, dados),

  enviar: (id: string) => api.patch<OrcamentoResponse>(`/orcamentos/${id}/enviar`, {}),

  aprovar: (id: string) => api.patch<OrcamentoResponse>(`/orcamentos/${id}/aprovar`, {}),

  recusar: (id: string) => api.patch<OrcamentoResponse>(`/orcamentos/${id}/recusar`, {}),

  cancelar: (id: string) => api.patch<OrcamentoResponse>(`/orcamentos/${id}/cancelar`, {}),
};
