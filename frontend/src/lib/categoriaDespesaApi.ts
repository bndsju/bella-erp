import { api } from "./api";
import type { CategoriaDespesaRequest, CategoriaDespesaResponse, StatusCategoriaDespesa } from "@/types/categoriaDespesa";

export const categoriaDespesaApi = {
  listar: (status?: StatusCategoriaDespesa) =>
    api.get<CategoriaDespesaResponse[]>(`/categorias-despesa${status ? `?status=${status}` : ""}`),

  buscarPorId: (id: string) => api.get<CategoriaDespesaResponse>(`/categorias-despesa/${id}`),

  cadastrar: (dados: CategoriaDespesaRequest) => api.post<CategoriaDespesaResponse>("/categorias-despesa", dados),

  editar: (id: string, dados: CategoriaDespesaRequest) =>
    api.put<CategoriaDespesaResponse>(`/categorias-despesa/${id}`, dados),

  alterarStatus: (id: string, status: StatusCategoriaDespesa) =>
    api.patch<CategoriaDespesaResponse>(`/categorias-despesa/${id}/status`, { status }),
};
