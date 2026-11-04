import { createContext, useContext } from "react";

/**
 * Usuário logado. O sistema ainda não tem login: enquanto isso o nome é nulo e a saudação do Início
 * aparece sem nome. Quando o login existir, basta fornecer o nome neste contexto (ex.: "Carlos") e a
 * tela passa a exibir "Boa tarde, Carlos" sem outras mudanças.
 */
export interface UsuarioAtual {
  nome: string | null;
}

export const UsuarioContexto = createContext<UsuarioAtual>({ nome: null });

export function useUsuarioAtual(): UsuarioAtual {
  return useContext(UsuarioContexto);
}

/** "Bom dia", "Boa tarde" ou "Boa noite", pela hora local; com nome, "Boa tarde, Carlos". */
export function saudacao(nome: string | null, agora: Date = new Date()): string {
  const hora = agora.getHours();
  const base = hora < 12 ? "Bom dia" : hora < 18 ? "Boa tarde" : "Boa noite";
  return nome ? `${base}, ${nome}` : base;
}
