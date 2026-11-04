import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
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
import { VendaStatusBadge } from "@/components/vendas/VendaStatusBadge";
import { vendaApi } from "@/lib/vendaApi";
import { clienteApi } from "@/lib/clienteApi";
import { opcoesDeClientes } from "@/lib/clienteOpcoes";
import { formatarMoeda } from "@/lib/formatos";
import { labelFormaPagamento } from "@/lib/formaPagamento";
import type { StatusVenda, VendaResponse } from "@/types/venda";
import type { ClienteResponse } from "@/types/cliente";

export function VendasListPage() {
  const navigate = useNavigate();
  const [vendas, setVendas] = useState<VendaResponse[]>([]);
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [clienteId, setClienteId] = useState<string>("TODOS");
  const [status, setStatus] = useState<StatusVenda | "TODOS">("TODOS");

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => {});
  }, []);

  const opcoesClientes = useMemo(() => opcoesDeClientes(clientes), [clientes]);

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

  const temFiltro = clienteId !== "TODOS" || status !== "TODOS";

  return (
    <>
      <CabecalhoPagina titulo="Vendas" />

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
            <Select value={status} onValueChange={(v) => setStatus(v as StatusVenda | "TODOS")}>
              <SelectTrigger id="filtro-status" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODOS">Todos os status</SelectItem>
                <SelectItem value="CONCLUIDA">Concluída</SelectItem>
                <SelectItem value="CANCELADA">Cancelada</SelectItem>
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
              <TableHead>Forma de pagamento</TableHead>
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
            ) : vendas.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5}>
                  <div className="vazio">
                    <h3>{temFiltro ? "Nenhuma venda com esses filtros" : "Nenhuma venda ainda"}</h3>
                    <p>{temFiltro ? "Ajuste ou limpe os filtros para ver mais." : "Sua primeira venda aparece aqui."}</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              vendas.map((venda) => (
                <TableRow key={venda.id} onClick={() => navigate(`/vendas/${venda.id}`)}>
                  <TableCell className="tabela__forte">{venda.clienteNome}</TableCell>
                  <TableCell>{labelFormaPagamento(venda.formaPagamento)}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(venda.valorTotal)}</TableCell>
                  <TableCell>
                    <VendaStatusBadge status={venda.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes itens={[{ rotulo: "Abrir", icone: ExternalLink, para: `/vendas/${venda.id}` }]} />
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
