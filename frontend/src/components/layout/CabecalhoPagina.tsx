import { createContext, useContext, type ReactNode } from "react";
import { createPortal } from "react-dom";

/** Pontos da barra e da faixa verde onde cada página coloca seu título, frase e ação principal. */
export interface EncaixesLayout {
  titulo: HTMLElement | null;
  frase: HTMLElement | null;
  acao: HTMLElement | null;
}

export const EncaixesContexto = createContext<EncaixesLayout>({ titulo: null, frase: null, acao: null });

interface CabecalhoPaginaProps {
  titulo: string;
  /** Frase curta sob o título, na faixa verde. */
  frase?: string;
  /** Ação principal da tela: vai para a barra superior, à direita. */
  acao?: ReactNode;
}

/**
 * Cabeçalho da tela. Em vez de cada página desenhar o seu título, ele é projetado na faixa verde do
 * layout, e a ação principal na barra superior, como define a direção criativa.
 */
export function CabecalhoPagina({ titulo, frase, acao }: CabecalhoPaginaProps) {
  const encaixes = useContext(EncaixesContexto);
  return (
    <>
      {encaixes.titulo && createPortal(titulo, encaixes.titulo)}
      {encaixes.frase && createPortal(frase ?? "", encaixes.frase)}
      {encaixes.acao && acao && createPortal(acao, encaixes.acao)}
    </>
  );
}
