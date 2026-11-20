import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { toast } from "sonner";
import { authApi } from "@/lib/authApi";
import { AuthContexto, type Autenticacao } from "@/lib/authContexto";
import { permissaoApi } from "@/lib/permissaoApi";
import { podeAcessarRota, temPermissao } from "@/lib/permissoes";
import { EVENTO_SESSAO_EXPIRADA, guardarToken, lerToken, limparToken } from "@/lib/sessao";
import { UsuarioContexto } from "@/lib/usuario";
import type { AcaoPermissao, Permissoes } from "@/types/permissao";
import type { UsuarioResponse } from "@/types/usuario";

const SEM_PERMISSOES: Permissoes = {};

/** Permissões de um usuário específico (para nunca usar as de quem estava logado antes). */
interface PermissoesDoUsuario {
  usuarioId: string;
  permissoes: Permissoes;
}

/**
 * Guarda quem está logado e o que ele pode fazer. Ao abrir, confirma o token no servidor; se não valer mais, volta
 * ao login. As permissões são relidas quando a aba volta ao foco, para refletir mudanças feitas pelo administrador.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null);
  const [carregandoSessao, setCarregandoSessao] = useState(() => lerToken() !== null);
  const [dadosPermissoes, setDadosPermissoes] = useState<PermissoesDoUsuario | null>(null);

  useEffect(() => {
    if (lerToken() === null) return;
    let ativo = true;
    authApi
      .eu()
      .then((eu) => ativo && setUsuario(eu))
      .catch(() => ativo && limparToken())
      .finally(() => ativo && setCarregandoSessao(false));
    return () => {
      ativo = false;
    };
  }, []);

  useEffect(() => {
    const aoExpirar = () => setUsuario(null);
    window.addEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
    return () => window.removeEventListener(EVENTO_SESSAO_EXPIRADA, aoExpirar);
  }, []);

  // O administrador pode tudo e quem tem senha provisória ainda não usa o sistema: nenhum dos dois precisa da lista.
  const precisaPermissoes = usuario !== null && usuario.perfil !== "ADMINISTRADOR" && !usuario.senhaProvisoria;
  const usuarioId = usuario?.id;

  const carregarPermissoes = useCallback((id: string, avisarErro: boolean) => {
    permissaoApi
      .minhas()
      .then((resposta) => setDadosPermissoes({ usuarioId: id, permissoes: resposta.permissoes }))
      .catch(() => {
        // sem a lista, o mais seguro é não mostrar nada que dependa dela
        setDadosPermissoes((atual) => atual ?? { usuarioId: id, permissoes: SEM_PERMISSOES });
        if (avisarErro) toast.error("Não foi possível carregar as suas permissões");
      });
  }, []);

  useEffect(() => {
    if (!precisaPermissoes || !usuarioId) return;
    carregarPermissoes(usuarioId, true);
    const aoVoltarAoFoco = () => {
      if (document.visibilityState === "visible") carregarPermissoes(usuarioId, false);
    };
    document.addEventListener("visibilitychange", aoVoltarAoFoco);
    return () => document.removeEventListener("visibilitychange", aoVoltarAoFoco);
  }, [precisaPermissoes, usuarioId, carregarPermissoes]);

  const entrar = useCallback(async (login: string, senha: string) => {
    const sessao = await authApi.login(login, senha);
    guardarToken(sessao.token);
    setUsuario(sessao.usuario);
  }, []);

  const sair = useCallback(() => {
    limparToken();
    setUsuario(null);
    setDadosPermissoes(null);
  }, []);

  const permissoes =
    usuarioId && dadosPermissoes?.usuarioId === usuarioId ? dadosPermissoes.permissoes : SEM_PERMISSOES;
  const aguardandoPermissoes = precisaPermissoes && dadosPermissoes?.usuarioId !== usuarioId;

  const valor = useMemo<Autenticacao>(
    () => ({
      usuario,
      carregando: carregandoSessao || aguardandoPermissoes,
      entrar,
      sair,
      pode: (modulo: string, acao: AcaoPermissao = "VER") =>
        usuario !== null && temPermissao(usuario.perfil, permissoes, modulo, acao),
      podeAcessarRota: (caminho: string) =>
        usuario !== null && podeAcessarRota(usuario.perfil, permissoes, caminho),
    }),
    [usuario, carregandoSessao, aguardandoPermissoes, entrar, sair, permissoes],
  );
  const nomeParaSaudacao = useMemo(() => ({ nome: usuario?.nome.split(" ")[0] ?? null }), [usuario]);

  return (
    <AuthContexto.Provider value={valor}>
      <UsuarioContexto.Provider value={nomeParaSaudacao}>{children}</UsuarioContexto.Provider>
    </AuthContexto.Provider>
  );
}
