import { api } from "./api";
import type { SessaoResponse, UsuarioResponse } from "@/types/usuario";

export const authApi = {
  login: (login: string, senha: string) => api.post<SessaoResponse>("/auth/login", { login, senha }),
  esqueciSenha: (login: string) => api.post<{ mensagem: string }>("/auth/esqueci-senha", { login }),
  eu: () => api.get<UsuarioResponse>("/auth/me"),
  alterarSenha: (senhaAtual: string, novaSenha: string) =>
    api.put<void>("/auth/senha", { senhaAtual, novaSenha }),
};
