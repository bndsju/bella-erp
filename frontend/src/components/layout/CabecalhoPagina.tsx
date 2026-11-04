import { createContext, useContext, useEffect, type ReactNode } from "react";
import { createPortal } from "react-dom";

/** Pontos da faixa verde onde cada página coloca seu título, frase e ação principal. */
export interface EncaixesLayout {
  titulo: HTMLElement | null;
  frase: HTMLElement | null;
  acao: HTMLElement | null;
  /** A faixa alta (40/112) é só do painel; as demais telas usam a faixa baixa (32/84). */
  definirFaixaBaixa: (baixa: boolean) => void;
}

export const EncaixesContexto = createContext<EncaixesLayout>({
  titulo: null,
  frase: null,
  acao: null,
  definirFaixaBaixa: () => {},
});

interface CabecalhoPaginaProps {
  titulo: string;
  /** Frase curta sob o título, na faixa verde. */
  frase?: string;
  /** Ação principal da tela: vai para a faixa verde, à direita, no botão preto. */
  acao?: ReactNode;
  /** Painel (Início) usa a faixa alta. */
  faixaAlta?: boolean;
}

/**
 * Cabeçalho da tela. Em vez de cada página desenhar o seu título, ele é projetado na faixa verde do
 * layout, como define o arquivo-mestre: título de 34 px, frase de 17 px e a ação principal à direita.
 */
export function CabecalhoPagina({ titulo, frase, acao, faixaAlta = false }: CabecalhoPaginaProps) {
  const encaixes = useContext(EncaixesContexto);
  const { definirFaixaBaixa } = encaixes;

  useEffect(() => {
    definirFaixaBaixa(!faixaAlta);
  }, [faixaAlta, definirFaixaBaixa]);

  useEffect(() => {
    document.title = `${titulo} · Bella`;
  }, [titulo]);

  return (
    <>
      {encaixes.titulo && createPortal(titulo, encaixes.titulo)}
      {encaixes.frase && createPortal(frase ?? "", encaixes.frase)}
      {encaixes.acao && acao && createPortal(acao, encaixes.acao)}
    </>
  );
}
