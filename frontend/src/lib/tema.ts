import { useCallback, useEffect, useState } from "react";

/** Escolha do usuário: claro (padrão), escuro ou a preferência do aparelho. */
export type PreferenciaTema = "sistema" | "claro" | "dark";

const CHAVE = "bella-tema";
const CONSULTA_ESCURO = "(prefers-color-scheme: dark)";

export function lerPreferencia(): PreferenciaTema {
  try {
    const salvo = localStorage.getItem(CHAVE);
    return salvo === "dark" || salvo === "sistema" ? salvo : "claro";
  } catch {
    return "claro";
  }
}

export function temaEfetivo(preferencia: PreferenciaTema): "claro" | "dark" {
  if (preferencia === "sistema") {
    return window.matchMedia(CONSULTA_ESCURO).matches ? "dark" : "claro";
  }
  return preferencia;
}

/** Aplica o tema no <html>. O index.html faz o mesmo antes de a página aparecer, para não piscar o claro. */
export function aplicarTema(preferencia: PreferenciaTema): void {
  const raiz = document.documentElement;
  if (temaEfetivo(preferencia) === "dark") {
    raiz.setAttribute("data-tema", "dark");
  } else {
    raiz.removeAttribute("data-tema");
  }
}

export function useTema() {
  const [preferencia, definir] = useState<PreferenciaTema>(lerPreferencia);
  const [efetivo, setEfetivo] = useState<"claro" | "dark">(() => temaEfetivo(lerPreferencia()));

  useEffect(() => {
    aplicarTema(preferencia);
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setEfetivo(temaEfetivo(preferencia));
    if (preferencia !== "sistema") return;
    const consulta = window.matchMedia(CONSULTA_ESCURO);
    const aoMudar = () => {
      aplicarTema("sistema");
      setEfetivo(temaEfetivo("sistema"));
    };
    consulta.addEventListener("change", aoMudar);
    return () => consulta.removeEventListener("change", aoMudar);
  }, [preferencia]);

  const escolher = useCallback((nova: PreferenciaTema) => {
    try {
      localStorage.setItem(CHAVE, nova);
    } catch {
      // sem armazenamento disponível, a escolha vale só até recarregar
    }
    definir(nova);
    window.dispatchEvent(new CustomEvent("bella-tema"));
  }, []);

  useEffect(() => {
    const aoEscolherEmOutroLugar = () => definir(lerPreferencia());
    window.addEventListener("bella-tema", aoEscolherEmOutroLugar);
    return () => window.removeEventListener("bella-tema", aoEscolherEmOutroLugar);
  }, []);

  return { preferencia, efetivo, escolher };
}
