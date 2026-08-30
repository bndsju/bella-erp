import { Badge } from "@/components/ui/badge";
import type { StatusCliente } from "@/types/cliente";

export function StatusBadge({ status }: { status: StatusCliente }) {
  return (
    <Badge variant={status === "ATIVO" ? "default" : "outline"}>
      {status === "ATIVO" ? "Ativo" : "Inativo"}
    </Badge>
  );
}
