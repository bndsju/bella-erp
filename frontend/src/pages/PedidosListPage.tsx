import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ExternalLink } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { SelecaoPesquisavel } from "@/components/ui/selecao-pesquisavel";
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
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { PedidoStatusBadge } from "@/components/pedidos/PedidoStatusBadge";
import { CriarPedidoDeOrcamentoDialog } from "@/components/pedidos/CriarPedidoDeOrcamentoDialog";
import { pedidoApi } from "@/lib/pedidoApi";
import { clienteApi } from "@/lib/clienteApi";
import { opcoesDeClientes } from "@/lib/clienteOpcoes";
import { formatarMoeda } from "@/lib/formatos";
import type { PedidoResponse, StatusPedido } from "@/types/pedido";
import type { ClienteResponse } from "@/types/cliente";

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

  const opcoesClientes = useMemo(() => opcoesDeClientes(clientes), [clientes]);

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

  const temFiltro = clienteId !== "TODOS" || status !== "TODOS";

  return (
    <>
      <CabecalhoPagina
        titulo="Pedidos"
        acao={
          <>
            <Button variant="secondary" onClick={() => setDialogOrcamentoAberto(true)}>
              Criar a partir de orçamento
            </Button>
            <Button asChild>
              <Link to="/pedidos/novo">Novo pedido</Link>
            </Button>
          </>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-cliente">Cliente</label>
            <SelecaoPesquisavel
              id="filtro-cliente"
              value={clienteId}
              onChange={setClienteId}
              opcoes={opcoesClientes}
              valorTodos="TODOS"
              rotuloTodos="Todos os clientes"
              placeholderBusca="Buscar por nome ou empresa"
              textoVazio="Nenhum cliente encontrado"
            />
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusPedido | "TODOS")}>
              <SelectTrigger id="filtro-status" className="w-full">
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
          <Button
            variant="secondary"
            disabled={!temFiltro}
            onClick={() => {
              setClienteId("TODOS");
              setStatus("TODOS");
            }}
          >
            Limpar filtros
          </Button>
        </div>
      </Card>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Cliente</TableHead>
              <TableHead>Transportadora</TableHead>
              <TableHead className="text-right">Total</TableHead>
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
                <TableCell colSpan={5}>
                  <div className="vazio">
                    <h3>{temFiltro ? "Nenhum pedido com esses filtros" : "Nenhum pedido ainda"}</h3>
                    <p>{temFiltro ? "Ajuste ou limpe os filtros para ver mais." : "Seu primeiro pedido aparece aqui."}</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              pedidos.map((pedido) => (
                <TableRow key={pedido.id} onClick={() => navigate(`/pedidos/${pedido.id}/editar`)}>
                  <TableCell className="tabela__forte">{pedido.clienteNome}</TableCell>
                  <TableCell>{pedido.transportadoraNome ?? "—"}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(pedido.valorTotal)}</TableCell>
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
      </Card>

      <CriarPedidoDeOrcamentoDialog open={dialogOrcamentoAberto} onOpenChange={setDialogOrcamentoAberto} />
    </>
  );
}
