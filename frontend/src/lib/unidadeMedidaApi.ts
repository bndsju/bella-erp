import { api } from "./api";
import type { UnidadeMedidaRequest, UnidadeMedidaResponse, StatusUnidadeMedida } from "@/types/unidadeMedida";

export const unidadeMedidaApi = {
  listar: (status?: StatusUnidadeMedida) =>
    api.get<UnidadeMedidaResponse[]>(`/unidades-medida${status ? `?status=${status}` : ""}`),

  buscarPorId: (id: string) => api.get<UnidadeMedidaResponse>(`/unidades-medida/${id}`),

  cadastrar: (dados: UnidadeMedidaRequest) => api.post<UnidadeMedidaResponse>("/unidades-medida", dados),

  editar: (id: string, dados: UnidadeMedidaRequest) =>
    api.put<UnidadeMedidaResponse>(`/unidades-medida/${id}`, dados),

  alterarStatus: (id: string, status: StatusUnidadeMedida) =>
    api.patch<UnidadeMedidaResponse>(`/unidades-medida/${id}/status`, { status }),
};
