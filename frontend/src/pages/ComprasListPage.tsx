import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
import { CompraStatusBadge } from "@/components/compras/CompraStatusBadge";
import { compraApi } from "@/lib/compraApi";
import { fornecedorApi } from "@/lib/fornecedorApi";
import type { CompraResponse, StatusCompra } from "@/types/compra";
import type { FornecedorResponse } from "@/types/fornecedor";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function formatarData(data: string | null): string {
  return data ? new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR") : "—";
}

export function ComprasListPage() {
  const [compras, setCompras] = useState<CompraResponse[]>([]);
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [fornecedorId, setFornecedorId] = useState<string>("TODOS");
  const [status, setStatus] = useState<StatusCompra | "TODOS">("TODOS");
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");

  useEffect(() => {
    fornecedorApi.listar().then(setFornecedores).catch(() => {});
  }, []);

  const carregar = useCallback(() => {
    setCarregando(true);
    compraApi
      .listar({
        fornecedorId: fornecedorId === "TODOS" ? undefined : fornecedorId,
        status: status === "TODOS" ? undefined : status,
        dataInicio: dataInicio || undefined,
        dataFim: dataFim || undefined,
      })
      .then(setCompras)
      .catch(() => toast.error("Não foi possível carregar as compras"))
      .finally(() => setCarregando(false));
  }, [fornecedorId, status, dataInicio, dataFim]);

  useEffect(carregar, [carregar]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Compras</h1>
        <Button asChild>
          <Link to="/compras/novo">Nova compra</Link>
        </Button>
      </div>

      <div className="flex flex-wrap items-end gap-3">
        <Select value={fornecedorId} onValueChange={setFornecedorId}>
          <SelectTrigger className="w-56">
            <SelectValue placeholder="Fornecedor" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os fornecedores</SelectItem>
            {fornecedores.map((fornecedor) => (
              <SelectItem key={fornecedor.id} value={fornecedor.id}>
                {fornecedor.nome}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={status} onValueChange={(v) => setStatus(v as StatusCompra | "TODOS")}>
          <SelectTrigger className="w-64">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="RASCUNHO">Rascunho</SelectItem>
            <SelectItem value="CONFIRMADA">Confirmada (recebimento pendente)</SelectItem>
            <SelectItem value="RECEBIDA">Recebida</SelectItem>
            <SelectItem value="CANCELADA">Cancelada</SelectItem>
          </SelectContent>
        </Select>
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">Compra de</label>
          <Input type="date" className="w-40" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="space-y-1">
          <label className="text-xs text-muted-foreground">até</label>
          <Input type="date" className="w-40" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
      </div>

      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Fornecedor</TableHead>
              <TableHead>Data da compra</TableHead>
              <TableHead>Previsão de recebimento</TableHead>
              <TableHead className="text-right">Total</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={6}>
                    <Skeleton className="h-6 w-full" />
                  </TableCell>
                </TableRow>
              ))
            ) : compras.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="py-8 text-center text-muted-foreground">
                  Nenhuma compra encontrada.
                </TableCell>
              </TableRow>
            ) : (
              compras.map((compra) => (
                <TableRow key={compra.id}>
                  <TableCell className="font-medium">{compra.fornecedorNome}</TableCell>
                  <TableCell>{formatarData(compra.dataCompra)}</TableCell>
                  <TableCell>{formatarData(compra.previsaoRecebimento)}</TableCell>
                  <TableCell className="text-right">{formatoMoeda.format(compra.valorTotal)}</TableCell>
                  <TableCell>
                    <CompraStatusBadge status={compra.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" asChild>
                      <Link to={`/compras/${compra.id}`}>Abrir</Link>
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
