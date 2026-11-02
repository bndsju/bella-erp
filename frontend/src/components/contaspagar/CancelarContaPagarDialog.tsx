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

interface CancelarContaPagarDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  processando: boolean;
  onConfirmar: (motivo: string) => void;
}

export function CancelarContaPagarDialog({
  open,
  onOpenChange,
  processando,
  onConfirmar,
}: CancelarContaPagarDialogProps) {
  const [motivo, setMotivo] = useState("");

  useEffect(() => {
    if (open) setMotivo("");
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cancelar conta a pagar</DialogTitle>
          <DialogDescription>
            Só é possível cancelar contas sem pagamentos. Uma conta cancelada não aceita novos pagamentos.
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
            Cancelar conta
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
