import { useCallback, useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { MatrizPerfil } from "@/components/permissoes/MatrizPerfil";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ApiError } from "@/lib/api";
import { permissaoApi } from "@/lib/permissaoApi";
import { avisosDeDependencia, iguais } from "@/lib/permissaoRegras";
import { DESCRICAO_PERFIL, PERFIS, PERFIS_EDITAVEIS, ROTULO_PERFIL } from "@/lib/permissoes";
import { usuarioApi } from "@/lib/usuarioApi";
import type { MatrizPermissoes, Permissoes } from "@/types/permissao";
import type { PerfilUsuario } from "@/types/usuario";

/** Administrador define o que cada perfil pode fazer em cada módulo. As alterações só valem ao salvar. */
export function PermissoesPage() {
  const [matriz, setMatriz] = useState<MatrizPermissoes | null>(null);
  const [erro, setErro] = useState(false);
  // o que está salvo no servidor e o que está sendo editado, por perfil
  const [salvas, setSalvas] = useState<Partial<Record<PerfilUsuario, Permissoes>>>({});
  const [rascunhos, setRascunhos] = useState<Partial<Record<PerfilUsuario, Permissoes>>>({});
  const [usuariosPorPerfil, setUsuariosPorPerfil] = useState<Partial<Record<PerfilUsuario, number>>>({});
  const [perfilAtivo, setPerfilAtivo] = useState<PerfilUsuario>("GERENTE");
  const [salvando, setSalvando] = useState(false);
  const [confirmandoRestauracao, setConfirmandoRestauracao] = useState(false);

  const carregar = useCallback(() => {
    Promise.all([permissaoApi.matriz(), usuarioApi.listar()])
      .then(([m, usuarios]) => {
        const doServidor = Object.fromEntries(m.perfis.map((p) => [p.perfil, p.permissoes]));
        setMatriz(m);
        setSalvas(doServidor);
        setRascunhos(doServidor);
        const contagem: Partial<Record<PerfilUsuario, number>> = {};
        usuarios
          .filter((u) => u.status === "ATIVO")
          .forEach((u) => {
            contagem[u.perfil] = (contagem[u.perfil] ?? 0) + 1;
          });
        setUsuariosPorPerfil(contagem);
        setErro(false);
      })
      .catch(() => setErro(true));
  }, []);

  useEffect(carregar, [carregar]);

  const alterado = useCallback(
    (perfil: PerfilUsuario) => !iguais(rascunhos[perfil] ?? {}, salvas[perfil] ?? {}),
    [rascunhos, salvas],
  );
  const avisos = useMemo(
    () => (matriz ? avisosDeDependencia(rascunhos[perfilAtivo] ?? {}, matriz.modulos) : []),
    [matriz, rascunhos, perfilAtivo],
  );

  async function salvar() {
    setSalvando(true);
    try {
      const resposta = await permissaoApi.alterar(perfilAtivo, rascunhos[perfilAtivo] ?? {});
      setSalvas((s) => ({ ...s, [perfilAtivo]: resposta.permissoes }));
      setRascunhos((r) => ({ ...r, [perfilAtivo]: resposta.permissoes }));
      toast.success(`Permissões do perfil ${ROTULO_PERFIL[perfilAtivo]} salvas`);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Não foi possível salvar as permissões");
    } finally {
      setSalvando(false);
    }
  }

  async function restaurarPadrao() {
    setSalvando(true);
    try {
      const resposta = await permissaoApi.restaurarPadrao(perfilAtivo);
      setSalvas((s) => ({ ...s, [perfilAtivo]: resposta.permissoes }));
      setRascunhos((r) => ({ ...r, [perfilAtivo]: resposta.permissoes }));
      toast.success(`Perfil ${ROTULO_PERFIL[perfilAtivo]} voltou ao padrão`);
      setConfirmandoRestauracao(false);
    } catch (e) {
      toast.error(e instanceof ApiError ? e.message : "Não foi possível restaurar o padrão");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <>
      <CabecalhoPagina titulo="Permissões" frase="O que cada perfil pode fazer em cada módulo do sistema" />

      {erro ? (
        <Card>
          <div className="vazio">
            <h3>Não foi possível carregar as permissões</h3>
            <Button
              variant="secondary"
              onClick={() => {
                setErro(false);
                carregar();
              }}
            >
              Tentar de novo
            </Button>
          </div>
        </Card>
      ) : !matriz ? (
        <Card>
          <Skeleton className="h-40 w-full" />
        </Card>
      ) : (
        <Card>
          <Tabs value={perfilAtivo} onValueChange={(v) => setPerfilAtivo(v as PerfilUsuario)}>
            <TabsList>
              {PERFIS.map((perfil) => (
                <TabsTrigger key={perfil} value={perfil}>
                  {ROTULO_PERFIL[perfil]} ({usuariosPorPerfil[perfil] ?? 0})
                  {alterado(perfil) && <span aria-label="alterações não salvas"> •</span>}
                </TabsTrigger>
              ))}
            </TabsList>

            {PERFIS.map((perfil) => (
              <TabsContent key={perfil} value={perfil} className="space-y-4">
                <p>
                  {DESCRICAO_PERFIL[perfil]} Usuários ativos neste perfil: <strong>{usuariosPorPerfil[perfil] ?? 0}</strong>.
                </p>

                <MatrizPerfil
                  matriz={matriz}
                  permissoes={rascunhos[perfil] ?? {}}
                  somenteLeitura={!PERFIS_EDITAVEIS.includes(perfil)}
                  aoMudar={(novas) => setRascunhos((r) => ({ ...r, [perfil]: novas }))}
                />

                {PERFIS_EDITAVEIS.includes(perfil) && (
                  <>
                    {perfil === perfilAtivo && avisos.length > 0 && (
                      <ul role="status" className="space-y-1 font-semibold text-erro">
                        {avisos.map((aviso) => (
                          <li key={aviso}>⚠ {aviso}</li>
                        ))}
                      </ul>
                    )}
                    <p className="text-auxiliar">
                      Vale na hora para a API depois de salvar. Quem está com o sistema aberto vê o menu atualizado ao
                      voltar para a aba ou ao recarregar.
                    </p>
                    <div className="flex flex-wrap gap-2">
                      <Button onClick={salvar} disabled={!alterado(perfil) || salvando}>
                        {salvando ? "Salvando..." : "Salvar alterações"}
                      </Button>
                      <Button
                        variant="outline"
                        disabled={!alterado(perfil) || salvando}
                        onClick={() => setRascunhos((r) => ({ ...r, [perfil]: salvas[perfil] ?? {} }))}
                      >
                        Descartar
                      </Button>
                      <Button variant="outline" disabled={salvando} onClick={() => setConfirmandoRestauracao(true)}>
                        Restaurar padrão
                      </Button>
                    </div>
                  </>
                )}
              </TabsContent>
            ))}
          </Tabs>
        </Card>
      )}

      <Dialog open={confirmandoRestauracao} onOpenChange={setConfirmandoRestauracao}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Restaurar padrão</DialogTitle>
          </DialogHeader>
          <p>
            Voltar as permissões do perfil <strong>{ROTULO_PERFIL[perfilAtivo]}</strong> para as que o sistema trazia?
            Isso descarta as alterações já salvas e vale na hora.
          </p>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setConfirmandoRestauracao(false)}>
              Cancelar
            </Button>
            <Button type="button" onClick={restaurarPadrao} disabled={salvando}>
              Restaurar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
