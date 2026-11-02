import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";

interface CancelarMovimentacaoCaixaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  processando: boolean;
  onConfirmar: (motivo: string) => void;
}

export function CancelarMovimentacaoCaixaDialog({
  open,
  onOpenChange,
  processando,
  onConfirmar,
}: CancelarMovimentacaoCaixaDialogProps) {
  const [motivo, setMotivo] = useState("");

  useEffect(() => {
    if (open) setMotivo("");
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cancelar lançamento</DialogTitle>
          <DialogDescription>
            Só lançamentos manuais podem ser cancelados. Um lançamento cancelado deixa de contar no saldo, mas continua registrado.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2">
          <label className="text-sm font-medium">Motivo do cancelamento</label>
          <Textarea rows={3} value={motivo} onChange={(e) => setMotivo(e.target.value)} />
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Voltar
          </Button>
          <Button
            type="button"
            variant="destructive"
            disabled={!motivo.trim() || processando}
            onClick={() => onConfirmar(motivo.trim())}
          >
            Cancelar lançamento
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
