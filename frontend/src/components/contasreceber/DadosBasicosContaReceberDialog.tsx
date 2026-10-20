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
import { clienteApi } from "@/lib/clienteApi";
import { contaReceberApi } from "@/lib/contaReceberApi";
import { ApiError } from "@/lib/api";
import type { ClienteResponse } from "@/types/cliente";
import type { ContaReceberResponse } from "@/types/contaReceber";

const SEM_CLIENTE = "SEM_CLIENTE";

function nomeExibicaoCliente(cliente: ClienteResponse): string {
  return cliente.tipoPessoa === "PF" ? (cliente.nomeCompleto ?? "") : (cliente.razaoSocial ?? "");
}

interface DadosBasicosContaReceberDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  conta: ContaReceberResponse;
  onSalvo: (conta: ContaReceberResponse) => void;
}

export function DadosBasicosContaReceberDialog({ open, onOpenChange, conta, onSalvo }: DadosBasicosContaReceberDialogProps) {
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [descricao, setDescricao] = useState("");
  const [clienteId, setClienteId] = useState(SEM_CLIENTE);
  const [observacoes, setObservacoes] = useState("");
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!open) return;
    setDescricao(conta.descricao);
    setClienteId(conta.clienteId ?? SEM_CLIENTE);
    setObservacoes(conta.observacoes ?? "");
    clienteApi.listar({ status: "ATIVO" }).then(setClientes).catch(() => {});
  }, [open, conta]);

  async function salvar() {
    if (!descricao.trim()) {
      toast.error("Descrição é obrigatória");
      return;
    }
    setSalvando(true);
    try {
      const atualizada = await contaReceberApi.editarDadosBasicos(conta.id, {
        descricao: descricao.trim(),
        clienteId: clienteId === SEM_CLIENTE ? undefined : clienteId,
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
            Valor e parcelas não são alterados aqui. Contas com recebimentos só permitem editar estes dados.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Descrição</label>
            <Input value={descricao} onChange={(e) => setDescricao(e.target.value)} />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Cliente</label>
            <Select value={clienteId} onValueChange={setClienteId}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={SEM_CLIENTE}>Sem cliente</SelectItem>
                {clientes.map((cliente) => (
                  <SelectItem key={cliente.id} value={cliente.id}>
                    {nomeExibicaoCliente(cliente)}
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
