import { EVENTO_SESSAO_EXPIRADA, lerToken, limparToken } from "./sessao";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

export class ApiError extends Error {
  status: number;
  erros?: string[];

  constructor(status: number, message: string, erros?: string[]) {
    super(message);
    this.status = status;
    this.erros = erros;
  }
}

interface ErroApi {
  mensagem?: string;
  erros?: string[];
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = lerToken();
  const resposta = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options?.headers,
    },
  });

  if (resposta.status === 204) {
    return undefined as T;
  }

  const corpo = await resposta.json().catch(() => null);

  // 401 fora do login = sessão inválida. No login, 401 só quer dizer "senha errada" e a tela mostra a mensagem.
  if (resposta.status === 401 && path !== "/auth/login" && token) {
    limparToken();
    window.dispatchEvent(new Event(EVENTO_SESSAO_EXPIRADA));
  }

  if (!resposta.ok) {
    const erro = corpo as ErroApi | null;
    throw new ApiError(
      resposta.status,
      erro?.mensagem ?? "Erro inesperado ao comunicar com o servidor",
      erro?.erros,
    );
  }

  return corpo as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PATCH", body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
