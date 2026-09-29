import { api } from "./api";
import type {
  StatusTransportadora,
  TransportadoraRequest,
  TransportadoraResponse,
} from "@/types/transportadora";

export const transportadoraApi = {
  listar: (status?: StatusTransportadora) =>
    api.get<TransportadoraResponse[]>(`/transportadoras${status ? `?status=${status}` : ""}`),

  buscarPorId: (id: string) => api.get<TransportadoraResponse>(`/transportadoras/${id}`),

  cadastrar: (dados: TransportadoraRequest) =>
    api.post<TransportadoraResponse>("/transportadoras", dados),

  editar: (id: string, dados: TransportadoraRequest) =>
    api.put<TransportadoraResponse>(`/transportadoras/${id}`, dados),

  alterarStatus: (id: string, status: StatusTransportadora) =>
    api.patch<TransportadoraResponse>(`/transportadoras/${id}/status`, { status }),
};
