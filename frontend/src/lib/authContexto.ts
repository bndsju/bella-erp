import { createContext, useContext } from "react";
import type { UsuarioResponse } from "@/types/usuario";

export interface Autenticacao {
  /** Usuário logado; nulo quando não há sessão. */
  usuario: UsuarioResponse | null;
  /** Verificando, ao abrir o sistema, se o token guardado ainda vale. */
  carregando: boolean;
  entrar: (login: string, senha: string) => Promise<void>;
  sair: () => void;
}

export const AuthContexto = createContext<Autenticacao | null>(null);

export function useAuth(): Autenticacao {
  const contexto = useContext(AuthContexto);
  if (!contexto) {
    throw new Error("useAuth precisa estar dentro do AuthProvider");
  }
  return contexto;
}
