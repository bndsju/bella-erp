import { useCallback, useEffect, useState } from "react";
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
import { Badge } from "@/components/ui/badge";
import { FornecedorFormDialog } from "@/components/fornecedores/FornecedorFormDialog";
import { fornecedorApi } from "@/lib/fornecedorApi";
import type { StatusFornecedor, FornecedorResponse } from "@/types/fornecedor";
import { CircleCheck, Pencil, Power } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

export function FornecedoresPage() {
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [status, setStatus] = useState<StatusFornecedor | "TODOS">("TODOS");
  const [dialogAberto, setDialogAberto] = useState(false);
  const [fornecedorEmEdicao, setFornecedorEmEdicao] = useState<FornecedorResponse | null>(null);

  const carregar = useCallback(() => {
    setCarregando(true);
    fornecedorApi
      .listar(status === "TODOS" ? undefined : status)
      .then(setFornecedores)
      .catch(() => toast.error("Não foi possível carregar os fornecedores"))
      .finally(() => setCarregando(false));
  }, [status]);

  useEffect(carregar, [carregar]);

  async function alternarStatus(fornecedor: FornecedorResponse) {
    const novoStatus = fornecedor.status === "ATIVO" ? "INATIVO" : "ATIVO";
    try {
      await fornecedorApi.alterarStatus(fornecedor.id, novoStatus);
      toast.success(novoStatus === "ATIVO" ? "Fornecedor ativado" : "Fornecedor inativado");
      carregar();
    } catch {
      toast.error("Não foi possível alterar o status do fornecedor");
    }
  }

  return (
    <div className="space-y-4">
      <CabecalhoPagina
        titulo="Fornecedores"
        acao={
          <>
          <Button
            onClick={() => {
              setFornecedorEmEdicao(null);
              setDialogAberto(true);
            }}
          >
            Novo fornecedor
          </Button>
          </>
        }
      />

      <div className="cartao">
  <Select value={status} onValueChange={(v) => setStatus(v as StatusFornecedor | "TODOS")}>
          <SelectTrigger className="w-40">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os status</SelectItem>
            <SelectItem value="ATIVO">Ativo</SelectItem>
            <SelectItem value="INATIVO">Inativo</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-cartao bg-card p-2">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome</TableHead>
              <TableHead>Telefone</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={4}>
                  <Skeleton className="h-6 w-full" />
                </TableCell>
              </TableRow>
            ) : fornecedores.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="py-8 text-center text-muted-foreground">
                  Nenhum fornecedor encontrado.
                </TableCell>
              </TableRow>
            ) : (
              fornecedores.map((fornecedor) => (
                <TableRow key={fornecedor.id}>
                  <TableCell className="font-medium">{fornecedor.nome}</TableCell>
                  <TableCell>{fornecedor.telefone ?? "—"}</TableCell>
                  <TableCell>
                    <Badge variant={fornecedor.status === "ATIVO" ? "default" : "outline"}>
                      {fornecedor.status === "ATIVO" ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <MenuAcoes
                      itens={[
                        {
                          rotulo: "Editar",
                          icone: Pencil,
                          aoClicar: () => {
                            setFornecedorEmEdicao(fornecedor);
                            setDialogAberto(true);
                          },
                        },
                        {
                          rotulo: fornecedor.status === "ATIVO" ? "Inativar" : "Ativar",
                          icone: fornecedor.status === "ATIVO" ? Power : CircleCheck,
                          perigo: fornecedor.status === "ATIVO",
                          aoClicar: () => alternarStatus(fornecedor),
                        },
                      ]}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <FornecedorFormDialog
        open={dialogAberto}
        onOpenChange={setDialogAberto}
        fornecedor={fornecedorEmEdicao}
        onSalvo={carregar}
      />
    </div>
  );
}
