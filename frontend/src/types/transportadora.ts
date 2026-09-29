export type StatusTransportadora = "ATIVO" | "INATIVO";

export interface TransportadoraResponse {
  id: string;
  nome: string;
  telefone: string | null;
  status: StatusTransportadora;
  criadoEm: string;
  atualizadoEm: string;
}

export interface TransportadoraRequest {
  nome: string;
  telefone?: string;
}
