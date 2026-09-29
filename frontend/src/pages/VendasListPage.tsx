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
import { VendaStatusBadge } from "@/components/vendas/VendaStatusBadge";
import { vendaApi } from "@/lib/vendaApi";
import { clienteApi } from "@/lib/clienteApi";
import { labelFormaPagamento } from "@/lib/formaPagamento";
import type { StatusVenda, VendaResponse } from "@/types/venda";
import type { ClienteResponse } from "@/types/cliente";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

export function VendasListPage() {
  const [vendas, setVendas] = useState<VendaResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [clienteId, setClienteId] = useState<string>("TODOS");
  const [status, setStatus] = useState<StatusVenda | "TODOS">("TODOS");

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    vendaApi
      .listar({
        clienteId: clienteId === "TODOS" ? undefined : clienteId,
        status: status === "TODOS" ? undefined : status,
      })
      .then(setVendas)
      .catch(() => toast.error("Não foi possível carregar as vendas"))
      .finally(() => setCarregando(false));
  }, [clienteId, status]);

  useEffect(carregar, [carregar]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Vendas</h1>
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
        <Select value={status} onValueChange={(v) => setStatus(v as StatusVenda | "TODOS")}>
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="CONCLUIDA">Concluída</SelectItem>
            <SelectItem value="CANCELADA">Cancelada</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Cliente</TableHead>
              <TableHead>Forma de pagamento</TableHead>
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
            ) : vendas.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-8 text-center text-muted-foreground">
                  Nenhuma venda encontrada.
                </TableCell>
              </TableRow>
            ) : (
              vendas.map((venda) => (
                <TableRow key={venda.id}>
                  <TableCell className="font-medium">{venda.clienteNome}</TableCell>
                  <TableCell>{labelFormaPagamento(venda.formaPagamento)}</TableCell>
                  <TableCell>{formatoMoeda.format(venda.valorTotal)}</TableCell>
                  <TableCell>
                    <VendaStatusBadge status={venda.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link to={`/vendas/${venda.id}`}>Abrir</Link>
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
