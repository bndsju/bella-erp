import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
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
import { ExternalLink } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function formatarData(data: string | null): string {
  return data ? new Date(`${data}T00:00:00`).toLocaleDateString("pt-BR") : "—";
}

export function ComprasListPage() {
  const navigate = useNavigate();
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
    <>
      <CabecalhoPagina
        titulo="Compras"
        acao={
          <>
          <Button asChild>
            <Link to="/compras/novo">Nova compra</Link>
          </Button>
          </>
        }
      />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="filtro-fornecedor">Fornecedor</label>
            <Select value={fornecedorId} onValueChange={setFornecedorId}>
            <SelectTrigger id="filtro-fornecedor" className="w-full">
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
          </div>
          <div className="campo">
            <label htmlFor="filtro-status">Status</label>
            <Select value={status} onValueChange={(v) => setStatus(v as StatusCompra | "TODOS")}>
            <SelectTrigger id="filtro-status" className="w-full">
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
          </div>
        <div className="campo">
          <label htmlFor="filtro-compra-de">Compra de</label>
          <Input id="filtro-compra-de" type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="campo">
          <label htmlFor="filtro-ate">Até</label>
          <Input id="filtro-ate" type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
        </div>
      </Card>

      <Card>
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
                <TableCell colSpan={6}>
                  <div className="vazio">
                    <h3>Nenhuma compra encontrada</h3>
                    <p>Ajuste os filtros ou cadastre um novo registro.</p>
                  </div>
                </TableCell>
              </TableRow>
            ) : (
              compras.map((compra) => (
                <TableRow key={compra.id} className="cursor-pointer" onClick={() => navigate(`/compras/${compra.id}`)}>
                  <TableCell className="tabela__forte">{compra.fornecedorNome}</TableCell>
                  <TableCell>{formatarData(compra.dataCompra)}</TableCell>
                  <TableCell>{formatarData(compra.previsaoRecebimento)}</TableCell>
                  <TableCell className="text-right">{formatoMoeda.format(compra.valorTotal)}</TableCell>
                  <TableCell>
                    <CompraStatusBadge status={compra.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes itens={[{ rotulo: "Abrir", icone: ExternalLink, para: `/compras/${compra.id}` }]} />
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
