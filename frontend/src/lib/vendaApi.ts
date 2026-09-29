import { api } from "./api";
import type { ConcluirVendaRequest, ListarVendasParams, VendaResponse } from "@/types/venda";

function queryString(params: ListarVendasParams): string {
  const query = new URLSearchParams();
  if (params.clienteId) query.set("clienteId", params.clienteId);
  if (params.status) query.set("status", params.status);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const vendaApi = {
  listar: (params: ListarVendasParams = {}) => api.get<VendaResponse[]>(`/vendas${queryString(params)}`),

  buscarPorId: (id: string) => api.get<VendaResponse>(`/vendas/${id}`),

  concluir: (dados: ConcluirVendaRequest) => api.post<VendaResponse>("/vendas", dados),

  cancelar: (id: string) => api.patch<VendaResponse>(`/vendas/${id}/cancelar`, {}),
};
