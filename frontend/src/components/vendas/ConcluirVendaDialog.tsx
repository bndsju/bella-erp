import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { vendaApi } from "@/lib/vendaApi";
import { ApiError } from "@/lib/api";
import { FORMAS_PAGAMENTO } from "@/lib/formaPagamento";
import type { FormaPagamento } from "@/types/venda";

interface ConcluirVendaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pedidoId: string;
}

export function ConcluirVendaDialog({ open, onOpenChange, pedidoId }: ConcluirVendaDialogProps) {
  const navigate = useNavigate();
  const [formaPagamento, setFormaPagamento] = useState<FormaPagamento | "">("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (open) setFormaPagamento("");
  }, [open]);

  async function confirmar() {
    if (!formaPagamento) {
      toast.error("Selecione a forma de pagamento");
      return;
    }
    setEnviando(true);
    try {
      const venda = await vendaApi.concluir({ pedidoId, formaPagamento });
      toast.success("Venda concluída");
      onOpenChange(false);
      navigate(`/vendas/${venda.id}`);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível concluir a venda");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Concluir venda</DialogTitle>
        </DialogHeader>
        <div className="space-y-2">
          <label className="text-sm font-medium">Forma de pagamento</label>
          <Select value={formaPagamento} onValueChange={(v) => setFormaPagamento(v as FormaPagamento)}>
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Selecione a forma de pagamento" />
            </SelectTrigger>
            <SelectContent>
              {FORMAS_PAGAMENTO.map((forma) => (
                <SelectItem key={forma.value} value={forma.value}>
                  {forma.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" disabled={enviando} onClick={confirmar}>
            Concluir venda
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
