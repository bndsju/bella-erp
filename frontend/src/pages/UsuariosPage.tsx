import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { CircleCheck, KeyRound, Pencil, Power } from "lucide-react";
import { toast } from "sonner";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { SenhaProvisoriaDialog } from "@/components/usuarios/SenhaProvisoriaDialog";
import { UsuarioFormDialog } from "@/components/usuarios/UsuarioFormDialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ApiError } from "@/lib/api";
import { useAuth } from "@/lib/authContexto";
import { PERFIS, ROTULO_PERFIL } from "@/lib/permissoes";
import { formatarDataHora } from "@/lib/relatorioRotulos";
import { usuarioApi } from "@/lib/usuarioApi";
import type { PerfilUsuario, StatusUsuario, UsuarioResponse } from "@/types/usuario";

/** Sem acento e em minúsculas, para "jose" achar "José". */
function normalizar(texto: string): string {
  return texto.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase();
}

export function UsuariosPage() {
  const { usuario: eu } = useAuth();
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [status, setStatus] = useState<StatusUsuario | "TODOS">("TODOS");
  const [perfil, setPerfil] = useState<PerfilUsuario | "TODOS">("TODOS");
  const [busca, setBusca] = useState("");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [emEdicao, setEmEdicao] = useState<UsuarioResponse | null>(null);
  const [comSenhaProvisoria, setComSenhaProvisoria] = useState<UsuarioResponse | null>(null);

  // Carrega todos e filtra na tela: os pedidos de nova senha aparecem no topo qualquer que seja o filtro.
  // A lista antiga fica na tela enquanto recarrega (depois de uma ação), sem piscar o "carregando".
  const carregar = useCallback(() => {
    usuarioApi
      .listar()
      .then(setUsuarios)
      .catch(() => toast.error("Não foi possível carregar os usuários"))
      .finally(() => setCarregando(false));
  }, []);

  const pedidos = useMemo(
    () =>
      usuarios
        .filter((u) => u.status === "ATIVO" && u.redefinicaoSolicitadaEm !== null)
        .sort((a, b) => (a.redefinicaoSolicitadaEm ?? "").localeCompare(b.redefinicaoSolicitadaEm ?? "")),
    [usuarios],
  );
  const visiveis = useMemo(() => {
    const termo = normalizar(busca.trim());
    return usuarios.filter(
      (u) =>
        (status === "TODOS" || u.status === status) &&
        (perfil === "TODOS" || u.perfil === perfil) &&
        (termo === "" || normalizar(u.nome).includes(termo) || normalizar(u.login).includes(termo)),
    );
  }, [usuarios, status, perfil, busca]);
  const filtrado = busca.trim() !== "" || status !== "TODOS" || perfil !== "TODOS";

  useEffect(carregar, [carregar]);

  async function alternarStatus(usuario: UsuarioResponse) {
    const novoStatus = usuario.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await usuarioApi.alterarStatus(usuario.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Usuário ativado" : "Usuário inativado: ele já foi desconectado");
      carregar();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível alterar o status do usuário");
    }
  }

  return (
    <>
      <CabecalhoPagina
        titulo="Usuários"
        frase="Quem pode entrar no sistema e com qual perfil"
        acao={
          <Button
            onClick={() => {
              setEmEdicao(null);
              setDialogAberto(true);
            }}
          >
            Novo usuário
          </Button>
        }
      />

      {pedidos.length > 0 && (
        <Card>
          <h2 className="mb-2">
            {pedidos.length === 1 ? "1 pedido de nova senha" : `${pedidos.length} pedidos de nova senha`}
          </h2>
          <p className="mb-3">
            Essas pessoas usaram "Esqueci a senha". Gere uma senha provisória e passe para elas; no primeiro acesso
            elas escolhem uma senha própria.
          </p>
          <ul className="space-y-2">
            {pedidos.map((u) => (
              <li key={u.id} className="flex flex-wrap items-center justify-between gap-2">
                <span>
                  <strong>{u.nome}</strong> ({u.login}) · pediu em {formatarDataHora(u.redefinicaoSolicitadaEm!)}
                </span>
                <Button size="sm" onClick={() => setComSenhaProvisoria(u)}>
                  Gerar senha provisória
                </Button>
              </li>
            ))}
          </ul>
        </Card>
      )}

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-busca">Buscar</label>
            <Input
              id="filtro-busca"
              placeholder="Nome ou login"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-perfil">Perfil</label>
            <Select value={perfil} onValueChange={(v) => setPerfil(v as PerfilUsuario | "TODOS")}>
              <SelectTrigger id="filtro-perfil" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos os perfis</SelectItem>
                {PERFIS.map((p) => (
                  <SelectItem key={p} value={p}>
                    {ROTULO_PERFIL[p]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusUsuario | "TODOS")}>
              <SelectTrigger id="filtro-status" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos os status</SelectItem>
                <SelectItem value="ATIVO">Ativo</SelectItem>
                <SelectItem value="INATIVO">Inativo</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {filtrado && (
            <Button
              variant="outline"
              onClick={() => {
                setBusca("");
                setPerfil("TODOS");
                setStatus("TODOS");
              }}
            >
              Limpar filtros
            </Button>
          )}
        </div>
        <p className="mt-3 text-auxiliar">
          {visiveis.length === usuarios.length
            ? `${usuarios.length} usuário(s)`
            : `${visiveis.length} de ${usuarios.length} usuário(s)`}
          {" · "}
          <Link to="/permissoes">Ver o que cada perfil pode fazer</Link>
        </p>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome</TableHead>
              <TableHead>Login</TableHead>
              <TableHead>Perfil</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Último acesso</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={6}>
                  <Skeleton className="h-6 w-full" />
                </TableCell>
              </TableRow>
            ) : visiveis.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6}>
                  <div className="vazio">
                    <h3>Nenhum usuário encontrado</h3>
                    <p>Ajuste a busca e os filtros ou cadastre um novo usuário.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              visiveis.map((usuario) => {
                const ehEu = usuario.id === eu?.id;
                return (
                  <TableRow key={usuario.id}>
                    <TableCell className="tabela__forte">
                      {usuario.nome}
                      {ehEu && (
                        <Badge variant="outline" className="ml-2">
                          Você
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>{usuario.login}</TableCell>
                    <TableCell>{ROTULO_PERFIL[usuario.perfil]}</TableCell>
                    <TableCell>
                      <Badge variant={usuario.status === "ATIVO" ? "default" : "outline"}>
                        {usuario.status === "ATIVO" ? "Ativo" : "Inativo"}
                      </Badge>
                      {usuario.redefinicaoSolicitadaEm && usuario.status === "ATIVO" && (
                        <Badge variant="outline" className="ml-2">
                          Pediu nova senha
                        </Badge>
                      )}
                      {usuario.senhaProvisoria && (
                        <Badge variant="outline" className="ml-2">
                          Senha provisória
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>{usuario.ultimoAcessoEm ? formatarDataHora(usuario.ultimoAcessoEm) : "Nunca entrou"}</TableCell>
                    <TableCell className="text-right">
                      <MenuAcoes
                        itens={[
                          {
                            rotulo: "Editar",
                            icone: Pencil,
                            aoClicar: () => {
                              setEmEdicao(usuario);
                              setDialogAberto(true);
                            },
                          },
                          {
                            rotulo: "Gerar senha provisória",
                            icone: KeyRound,
                            oculto: ehEu || usuario.status === "INATIVO", // a própria senha se troca em "Minha conta"
                            aoClicar: () => setComSenhaProvisoria(usuario),
                          },
                          {
                            rotulo: usuario.status === "ATIVO" ? "Inativar" : "Ativar",
                            icone: usuario.status === "ATIVO" ? Power : CircleCheck,
                            perigo: usuario.status === "ATIVO",
                            oculto: ehEu,
                            aoClicar: () => alternarStatus(usuario),
                          },
                        ]}
                      />
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </Card>

      <UsuarioFormDialog open={dialogAberto} onOpenChange={setDialogAberto} usuario={emEdicao} onSalvo={carregar} />
      <SenhaProvisoriaDialog
        usuario={comSenhaProvisoria}
        onOpenChange={(aberto) => !aberto && setComSenhaProvisoria(null)}
        onGerada={carregar}
      />
    </>
  );
}
