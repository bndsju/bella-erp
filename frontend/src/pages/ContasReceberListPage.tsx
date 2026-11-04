import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { StatusContaReceberBadge } from "@/components/contasreceber/StatusContaReceberBadge";
import { clienteApi } from "@/lib/clienteApi";
import { contaReceberApi } from "@/lib/contaReceberApi";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import type { ClienteResponse } from "@/types/cliente";
import type { ContaReceberResponse, SituacaoContaReceber, StatusContaReceber } from "@/types/contaReceber";
import { ExternalLink } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

function proximoVencimento(conta: ContaReceberResponse): string | null {
  const emAberto = conta.parcelas
    .filter((parcela) => parcela.status === "PENDENTE" || parcela.status === "PARCIALMENTE_RECEBIDA")
    .map((parcela) => parcela.vencimento)
    .sort();
  return emAberto[0] ?? null;
}

export function ContasReceberListPage() {
  const navigate = useNavigate();
  const [contas, setContas] = useState<ContaReceberResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [clienteId, setClienteId] = useState("TODOS");
  const [status, setStatus] = useState<StatusContaReceber | "TODOS">("TODOS");
  const [situacao, setSituacao] = useState<SituacaoContaReceber | "TODAS">("TODAS");
  const [vencimentoInicio, setVencimentoInicio] = useState("");
  const [vencimentoFim, setVencimentoFim] = useState("");

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    contaReceberApi
      .listar({
        clienteId: clienteId === "TODOS" ? undefined : clienteId,
        status: status === "TODOS" ? undefined : status,
        situacao: situacao === "TODAS" ? undefined : situacao,
        vencimentoInicio: vencimentoInicio || undefined,
        vencimentoFim: vencimentoFim || undefined,
      })
      .then(setContas)
      .catch(() => toast.error("Não foi possível carregar as contas a receber"))
      .finally(() => setCarregando(false));
  }, [clienteId, status, situacao, vencimentoInicio, vencimentoFim]);

  useEffect(carregar, [carregar]);

  const aReceber = contas.reduce((total, conta) => total + conta.saldoPendente, 0);
  const vencido = contas.reduce(
    (total, conta) =>
      total + conta.parcelas.filter((parcela) => parcela.vencida).reduce((soma, parcela) => soma + parcela.saldoPendente, 0),
    0,
  );
  const recebido = contas.reduce((total, conta) => total + conta.valorRecebido, 0);

  return (
    <>
      <CabecalhoPagina
        titulo="Contas a receber"
        acao={
          <>
          <Button asChild>
            <Link to="/contas-receber/novo">Nova conta</Link>
          </Button>
          </>
        }
      />

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">A receber (saldo pendente)</p>
            <p className="text-lg font-medium">{formatarMoeda(aReceber)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Vencido</p>
            <p className="text-lg font-medium text-destructive">{formatarMoeda(vencido)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Já recebido</p>
            <p className="text-lg font-medium text-primary">{formatarMoeda(recebido)}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-cliente">Cliente</label>
            <Select value={clienteId} onValueChange={setClienteId}>
            <SelectTrigger id="filtro-cliente" className="w-full">
            <SelectValue placeholder="Cliente" />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODOS">Todos os clientes</SelectItem>
            {clientes.map((cliente) => (
            <SelectItem key={cliente.id} value={cliente.id}>
            {nomeExibicaoCliente(cliente)}
            </SelectItem>
            ))}
            </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-situacao">Situação</label>
            <Select value={situacao} onValueChange={(v) => setSituacao(v as SituacaoContaReceber | "TODAS")}>
            <SelectTrigger id="filtro-situacao" className="w-full">
            <SelectValue />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODAS">Todas as situações</SelectItem>
            <SelectItem value="EM_ABERTO">Em aberto</SelectItem>
            <SelectItem value="VENCIDAS">Vencidas</SelectItem>
            <SelectItem value="RECEBIDAS">Recebidas</SelectItem>
            </SelectContent>
            </Select>
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusContaReceber | "TODOS")}>
            <SelectTrigger id="filtro-status" className="w-full">
            <SelectValue />
            </SelectTrigger>
            <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="PENDENTE">Pendente</SelectItem>
            <SelectItem value="PARCIALMENTE_RECEBIDA">Parcialmente recebida</SelectItem>
            <SelectItem value="RECEBIDA">Recebida</SelectItem>
            <SelectItem value="CANCELADA">Cancelada</SelectItem>
            </SelectContent>
            </Select>
          </div>
        <div className="campo">
          <label htmlFor="filtro-vencimento-de">Vencimento de</label>
          <Input id="filtro-vencimento-de" type="date" value={vencimentoInicio} onChange={(e) => setVencimentoInicio(e.target.value)} />
        </div>
        <div className="campo">
          <label htmlFor="filtro-ate">Até</label>
          <Input id="filtro-ate" type="date" value={vencimentoFim} onChange={(e) => setVencimentoFim(e.target.value)} />
        </div>
        </div>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Descrição</TableHead>
              <TableHead>Cliente</TableHead>
              <TableHead>Próx. vencimento</TableHead>
              <TableHead className="text-right">Total</TableHead>
              <TableHead className="text-right">Saldo</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={7}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : contas.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7}>
                  <div className="vazio">
                    <h3>Nenhuma conta a receber encontrada</h3>
                    <p>Ajuste os filtros ou cadastre um novo registro.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              contas.map((conta) => (
                <TableRow key={conta.id} className="cursor-pointer" onClick={() => navigate(`/contas-receber/${conta.id}`)}>
                  <TableCell className="tabela__forte">{conta.descricao}</TableCell>
                  <TableCell>{conta.clienteNome ?? "—"}</TableCell>
                  <TableCell>{formatarData(proximoVencimento(conta))}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(conta.valorTotal)}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(conta.saldoPendente)}</TableCell>
                  <TableCell>
                    <StatusContaReceberBadge status={conta.status} vencida={conta.vencida} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes itens={[{ rotulo: "Abrir", icone: ExternalLink, para: `/contas-receber/${conta.id}` }]} />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </Card>
    </>
  );
}
