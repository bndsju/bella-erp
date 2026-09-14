import { api } from "./api";
import type { CategoriaRequest, CategoriaResponse, StatusCategoria } from "@/types/categoria";

export const categoriaApi = {
  listar: (status?: StatusCategoria) =>
    api.get<CategoriaResponse[]>(`/categorias${status ? `?status=${status}` : ""}`),

  buscarPorId: (id: string) => api.get<CategoriaResponse>(`/categorias/${id}`),

  cadastrar: (dados: CategoriaRequest) => api.post<CategoriaResponse>("/categorias", dados),

  editar: (id: string, dados: CategoriaRequest) =>
    api.put<CategoriaResponse>(`/categorias/${id}`, dados),

  alterarStatus: (id: string, status: StatusCategoria) =>
    api.patch<CategoriaResponse>(`/categorias/${id}/status`, { status }),
};
