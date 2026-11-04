import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
import { PedidoStatusBadge } from "@/components/pedidos/PedidoStatusBadge";
import { CriarPedidoDeOrcamentoDialog } from "@/components/pedidos/CriarPedidoDeOrcamentoDialog";
import { pedidoApi } from "@/lib/pedidoApi";
import { clienteApi } from "@/lib/clienteApi";
import type { PedidoResponse, StatusPedido } from "@/types/pedido";
import type { ClienteResponse } from "@/types/cliente";
import { ExternalLink } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

export function PedidosListPage() {
  const navigate = useNavigate();
  const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [clienteId, setClienteId] = useState<string>("TODOS");
  const [status, setStatus] = useState<StatusPedido | "TODOS">("TODOS");
  const [dialogOrcamentoAberto, setDialogOrcamentoAberto] = useState(false);

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    pedidoApi
      .listar({
        clienteId: clienteId === "TODOS" ? undefined : clienteId,
        status: status === "TODOS" ? undefined : status,
      })
      .then(setPedidos)
      .catch(() => toast.error("Não foi possível carregar os pedidos"))
      .finally(() => setCarregando(false));
  }, [clienteId, status]);

  useEffect(carregar, [carregar]);

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Pedidos"
        acao={
          <>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setDialogOrcamentoAberto(true)}>
              Criar a partir de orçamento
            </Button>
            <Button asChild>
              <Link to="/pedidos/novo">Novo pedido</Link>
            </Button>
          </div>
          </>
        }
      />

      <div className="cartao flex flex-wrap items-end gap-3">
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
        <Select value={status} onValueChange={(v) => setStatus(v as StatusPedido | "TODOS")}>
          <SelectTrigger className="w-52">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="CRIADO">Criado</SelectItem>
            <SelectItem value="CONFIRMADO">Confirmado</SelectItem>
            <SelectItem value="EM_SEPARACAO">Em separação</SelectItem>
            <SelectItem value="PRONTO_PARA_ENTREGA">Pronto para entrega</SelectItem>
            <SelectItem value="ENTREGUE">Entregue</SelectItem>
            <SelectItem value="CANCELADO">Cancelado</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-cartao bg-card p-2">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Cliente</TableHead>
              <TableHead>Transportadora</TableHead>
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
            ) : pedidos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-8 text-center text-muted-foreground">
                  Nenhum pedido encontrado.
                </TableCell>
              </TableRow>
            ) : (
              pedidos.map((pedido) => (
                <TableRow key={pedido.id} className="cursor-pointer" onClick={() => navigate(`/pedidos/${pedido.id}/editar`)}>
                  <TableCell className="font-medium">{pedido.clienteNome}</TableCell>
                  <TableCell>{pedido.transportadoraNome ?? "—"}</TableCell>
                  <TableCell>{formatoMoeda.format(pedido.valorTotal)}</TableCell>
                  <TableCell>
                    <PedidoStatusBadge status={pedido.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes itens={[{ rotulo: "Abrir", icone: ExternalLink, para: `/pedidos/${pedido.id}/editar` }]} />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <CriarPedidoDeOrcamentoDialog open={dialogOrcamentoAberto} onOpenChange={setDialogOrcamentoAberto} />
    </div>
  );
}
