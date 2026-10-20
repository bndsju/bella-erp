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

interface CancelarCompraDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  processando: boolean;
  onConfirmar: (motivo: string) => void;
}

export function CancelarCompraDialog({ open, onOpenChange, processando, onConfirmar }: CancelarCompraDialogProps) {
  const [motivo, setMotivo] = useState("");

  useEffect(() => {
    if (open) setMotivo("");
  }, [open]);

  const motivoValido = motivo.trim().length > 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cancelar compra</DialogTitle>
          <DialogDescription>
            Uma compra cancelada não gera entrada no estoque e não pode voltar ao fluxo normal.
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
            disabled={!motivoValido || processando}
            onClick={() => onConfirmar(motivo.trim())}
          >
            Cancelar compra
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
