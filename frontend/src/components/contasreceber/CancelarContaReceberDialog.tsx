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

interface CancelarContaReceberDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  processando: boolean;
  onConfirmar: (motivo: string) => void;
}

export function CancelarContaReceberDialog({
  open,
  onOpenChange,
  processando,
  onConfirmar,
}: CancelarContaReceberDialogProps) {
  const [motivo, setMotivo] = useState("");

  useEffect(() => {
    if (open) setMotivo("");
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cancelar conta a receber</DialogTitle>
          <DialogDescription>
            Só é possível cancelar contas sem recebimentos. Uma conta cancelada não recebe novos pagamentos.
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
