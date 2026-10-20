import { api } from "./api";
import type { CompraRequest, CompraResponse, ListarComprasParams } from "@/types/compra";

function queryString(params: ListarComprasParams): string {
  const query = new URLSearchParams();
  if (params.fornecedorId) query.set("fornecedorId", params.fornecedorId);
  if (params.status) query.set("status", params.status);
  if (params.dataInicio) query.set("dataInicio", params.dataInicio);
  if (params.dataFim) query.set("dataFim", params.dataFim);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const compraApi = {
  listar: (params: ListarComprasParams = {}) => api.get<CompraResponse[]>(`/compras${queryString(params)}`),

  buscarPorId: (id: string) => api.get<CompraResponse>(`/compras/${id}`),

  cadastrar: (dados: CompraRequest) => api.post<CompraResponse>("/compras", dados),

  editar: (id: string, dados: CompraRequest) => api.put<CompraResponse>(`/compras/${id}`, dados),

  confirmar: (id: string) => api.patch<CompraResponse>(`/compras/${id}/confirmar`, {}),

  receber: (id: string, dataRecebimento?: string) =>
    api.patch<CompraResponse>(`/compras/${id}/receber`, dataRecebimento ? { dataRecebimento } : {}),

  cancelar: (id: string, motivo: string) => api.patch<CompraResponse>(`/compras/${id}/cancelar`, { motivo }),
};
