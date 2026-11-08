import { Button } from "@/components/ui/button";
import type { Pagina } from "@/types/relatorio";

interface PaginacaoProps {
  pagina: Pagina<unknown>;
  onMudar: (pagina: number) => void;
}

export function Paginacao({ pagina, onMudar }: PaginacaoProps) {
  if (pagina.totalItens === 0) return null;
  const total = Math.max(pagina.totalPaginas, 1);
  return (
    <div className="paginacao">
      <span>
        Página {pagina.pagina + 1} de {total} · {pagina.totalItens} {pagina.totalItens === 1 ? "registro" : "registros"}
      </span>
      <div className="flex gap-2">
        <Button variant="secondary" size="sm" disabled={pagina.pagina === 0} onClick={() => onMudar(pagina.pagina - 1)}>
          Anterior
        </Button>
        <Button
          variant="secondary"
          size="sm"
          disabled={pagina.pagina + 1 >= total}
          onClick={() => onMudar(pagina.pagina + 1)}
        >
          Próxima
        </Button>
      </div>
    </div>
  );
}
