import type { ReactNode } from "react";
import { useAuth } from "@/lib/authContexto";
import type { AcaoPermissao } from "@/types/permissao";

/** Só mostra o conteúdo a quem tem a permissão. Esconder é conforto: quem barra de verdade é a API. */
export function Pode({ modulo, acao = "VER", children }: { modulo: string; acao?: AcaoPermissao; children: ReactNode }) {
  const { pode } = useAuth();
  return pode(modulo, acao) ? <>{children}</> : null;
}
