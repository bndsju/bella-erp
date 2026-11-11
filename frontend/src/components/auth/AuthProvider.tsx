import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { authApi } from "@/lib/authApi";
import { AuthContexto, type Autenticacao } from "@/lib/authContexto";
import { EVENTO_SESSAO_EXPIRADA, guardarToken, lerToken, limparToken } from "@/lib/sessao";
import { UsuarioContexto } from "@/lib/usuario";
import type { UsuarioResponse } from "@/types/usuario";

/** Guarda quem está logado. Ao abrir, confirma o token guardado no servidor; se não valer mais, volta ao login. */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null);
  const [carregando, setCarregando] = useState(() => lerToken() !== null);

  useEffect(() => {
    if (lerToken() === null) return;
    let ativo = true;
    authApi
      .eu()
      .then((eu) => ativo && setUsuario(eu))
      .catch(() => ativo && limparToken())
      .finally(() => ativo && setCarregando(false));
    return () => {
      ativo = false;
    };
  }, []);

  useEffect(() => {
    const aoExpirar = () => setUsuario(null);
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
    return () => window.removeEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
  }, []);

  const entrar = useCallback(async (login: string, senha: string) => {
    const sessao = await authApi.login(login, senha);
    guardarToken(sessao.token);
    setUsuario(sessao.usuario);
  }, []);

  const sair = useCallback(() => {
    limparToken();
    setUsuario(null);
  }, []);

  const valor = useMemo<Autenticacao>(
    () => ({ usuario, carregando, entrar, sair }),
    [usuario, carregando, entrar, sair],
  );
  const nomeParaSaudacao = useMemo(() => ({ nome: usuario?.nome.split(" ")[0] ?? null }), [usuario]);

  return (
    <AuthContexto.Provider value={valor}>
      <UsuarioContexto.Provider value={nomeParaSaudacao}>{children}</UsuarioContexto.Provider>
    </AuthContexto.Provider>
  );
}
