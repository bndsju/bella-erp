import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
import { OrcamentoStatusBadge } from "@/components/orcamentos/OrcamentoStatusBadge";
import { orcamentoApi } from "@/lib/orcamentoApi";
import { clienteApi } from "@/lib/clienteApi";
import type { OrcamentoResponse, StatusOrcamento } from "@/types/orcamento";
import type { ClienteResponse } from "@/types/cliente";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

export function OrcamentosListPage() {
  const [orcamentos, setOrcamentos] = useState<OrcamentoResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [clienteId, setClienteId] = useState<string>("TODOS");
  const [status, setStatus] = useState<StatusOrcamento | "TODOS">("TODOS");

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    orcamentoApi
      .listar({
        clienteId: clienteId === "TODOS" ? undefined : clienteId,
        status: status === "TODOS" ? undefined : status,
      })
      .then(setOrcamentos)
      .catch(() => toast.error("Não foi possível carregar os orçamentos"))
      .finally(() => setCarregando(false));
  }, [clienteId, status]);

  useEffect(carregar, [carregar]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Orçamentos</h1>
        <Button asChild>
          <Link to="/orcamentos/novo">Novo orçamento</Link>
        </Button>
      </div>

      <div className="flex gap-3">
        <Select value={clienteId} onValueChange={setClienteId}>
          <SelectTrigger className="w-56">
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
        <Select value={status} onValueChange={(v) => setStatus(v as StatusOrcamento | "TODOS")}>
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="RASCUNHO">Rascunho</SelectItem>
            <SelectItem value="ENVIADO">Enviado</SelectItem>
            <SelectItem value="APROVADO">Aprovado</SelectItem>
            <SelectItem value="RECUSADO">Recusado</SelectItem>
            <SelectItem value="EXPIRADO">Expirado</SelectItem>
            <SelectItem value="CANCELADO">Cancelado</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Cliente</TableHead>
              <TableHead>Válido até</TableHead>
              <TableHead>Total</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={5}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : orcamentos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-8 text-center text-muted-foreground">
                  Nenhum orçamento encontrado.
                </TableCell>
              </TableRow>
            ) : (
              orcamentos.map((orcamento) => (
                <TableRow key={orcamento.id}>
                  <TableCell className="font-medium">{orcamento.clienteNome}</TableCell>
                  <TableCell>{new Date(orcamento.prazoValidade).toLocaleDateString("pt-BR")}</TableCell>
                  <TableCell>{formatoMoeda.format(orcamento.valorTotal)}</TableCell>
                  <TableCell>
                    <OrcamentoStatusBadge status={orcamento.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link to={`/orcamentos/${orcamento.id}/editar`}>Abrir</Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
