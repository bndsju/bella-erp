import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { fornecedorApi } from "@/lib/fornecedorApi";
import { categoriaDespesaApi } from "@/lib/categoriaDespesaApi";
import type { CategoriaDespesaResponse } from "@/types/categoriaDespesa";
import { contaPagarApi } from "@/lib/contaPagarApi";
import { ApiError } from "@/lib/api";
import type { FornecedorResponse } from "@/types/fornecedor";
import type { ContaPagarResponse } from "@/types/contaPagar";

const SEM_FORNECEDOR = "SEM_FORNECEDOR";

function nomeExibicaoFornecedor(fornecedor: FornecedorResponse): string {
  return fornecedor.nome;
}

interface DadosBasicosContaPagarDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  conta: ContaPagarResponse;
  onSalvo: (conta: ContaPagarResponse) => void;
}

export function DadosBasicosContaPagarDialog({ open, onOpenChange, conta, onSalvo }: DadosBasicosContaPagarDialogProps) {
  const [fornecedores, setFornecedores] = useState<FornecedorResponse[]>([]);
  const [categorias, setCategorias] = useState<CategoriaDespesaResponse[]>([]);
  const [categoriaDespesaId, setCategoriaDespesaId] = useState("");
  const [descricao, setDescricao] = useState("");
  const [fornecedorId, setFornecedorId] = useState(SEM_FORNECEDOR);
  const [observacoes, setObservacoes] = useState("");
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!open) return;
    setDescricao(conta.descricao);
    setFornecedorId(conta.fornecedorId ?? SEM_FORNECEDOR);
    setObservacoes(conta.observacoes ?? "");
    setCategoriaDespesaId(conta.categoriaDespesaId);
    categoriaDespesaApi.listar("ATIVO").then(setCategorias).catch(() => {});
    fornecedorApi.listar("ATIVO").then(setFornecedores).catch(() => {});
  }, [open, conta]);

  async function salvar() {
    if (!descricao.trim()) {
      toast.error("Descrição é obrigatória");
      return;
    }
    if (!categoriaDespesaId) {
      toast.error("Categoria de despesa é obrigatória");
      return;
    }
    setSalvando(true);
    try {
      const atualizada = await contaPagarApi.editarDadosBasicos(conta.id, {
        descricao: descricao.trim(),
        categoriaDespesaId,
        fornecedorId: fornecedorId === SEM_FORNECEDOR ? undefined : fornecedorId,
        observacoes: observacoes.trim() || undefined,
      });
      toast.success("Dados atualizados");
      onOpenChange(false);
      onSalvo(atualizada);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao atualizar os dados");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Editar dados da conta</DialogTitle>
          <DialogDescription>
            Valor e parcelas não são alterados aqui. Contas com pagamentos só permitem editar estes dados.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Descrição</label>
            <Input value={descricao} onChange={(e) => setDescricao(e.target.value)} />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Categoria de despesa</label>
            <Select value={categoriaDespesaId} onValueChange={setCategoriaDespesaId}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Selecione" />
              </SelectTrigger>
              <SelectContent>
                {categorias.map((categoria) => (
                  <SelectItem key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                  </SelectItem>
                ))}
                {!categorias.some((c) => c.id === conta.categoriaDespesaId) && (
                  <SelectItem value={conta.categoriaDespesaId}>{conta.categoriaDespesaNome}</SelectItem>
                )}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Fornecedor</label>
            <Select value={fornecedorId} onValueChange={setFornecedorId}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={SEM_FORNECEDOR}>Sem fornecedor</SelectItem>
                {fornecedores.map((fornecedor) => (
                  <SelectItem key={fornecedor.id} value={fornecedor.id}>
                    {nomeExibicaoFornecedor(fornecedor)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Observações</label>
            <Textarea rows={3} value={observacoes} onChange={(e) => setObservacoes(e.target.value)} />
          </div>
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" disabled={salvando} onClick={salvar}>
            Salvar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
