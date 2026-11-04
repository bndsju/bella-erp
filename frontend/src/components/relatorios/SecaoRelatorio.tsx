import { useCallback, useEffect, useRef, useState, type ReactNode } from "react";
import { FileDown } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { gerarPdf, type DocumentoPdf } from "@/lib/pdf";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

interface SecaoRelatorioProps<T> {
  titulo: string;
  /** Muda quando os filtros mudam; cada mudança refaz a consulta. */
  chave: string;
  buscar: () => Promise<T>;
  /** Filtros próprios da seção, mostrados sob o título. */
  filtros?: ReactNode;
  /** Monta o PDF deste relatório (com todas as páginas); sem isto, a seção não tem o botão de exportar. */
  exportar?: () => Promise<DocumentoPdf>;
  children: (dados: T) => ReactNode;
}

/** Cartão de um relatório: busca sozinho, mostra carregando/erro e entrega os dados a quem desenha. */
export function SecaoRelatorio<T>({ titulo, chave, buscar, filtros, exportar, children }: SecaoRelatorioProps<T>) {
  const [exportando, setExportando] = useState(false);
  const [dados, setDados] = useState<T | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [falhou, setFalhou] = useState(false);
  const buscarRef = useRef(buscar);
  const contador = useRef(0);

  useEffect(() => {
    buscarRef.current = buscar;
  });

  const carregar = useCallback(() => {
    const atual = ++contador.current;
    setCarregando(true);
    setFalhou(false);
    buscarRef
      .current()
      .then((resultado) => {
        if (atual === contador.current) setDados(resultado);
      })
      .catch(() => {
        if (atual === contador.current) setFalhou(true);
      })
      .finally(() => {
        if (atual === contador.current) setCarregando(false);
      });
  }, []);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(carregar, [carregar, chave]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>{titulo}</CardTitle>
        {exportar && (
          <Button
            variant="secondary"
            size="sm"
            disabled={exportando || !dados}
            onClick={() => {
              setExportando(true);
              exportar()
                .then(gerarPdf)
                .catch(() => toast.error("Não foi possível gerar o PDF"))
                .finally(() => setExportando(false));
            }}
          >
            <FileDown />
            {exportando ? "Gerando…" : "Exportar PDF"}
          </Button>
        )}
      </CardHeader>
      {filtros && <div className="barra-filtros mb-4">{filtros}</div>}
      {falhou && !dados ? (
        <div className="vazio">
          <h3>Não consegui carregar este relatório</h3>
          <p>Confira se o sistema está no ar e tente de novo.</p>
          <Button onClick={carregar}>Tentar de novo</Button>
        </div>
      ) : !dados ? (
        <Skeleton className="h-40 w-full rounded-[20px]" />
      ) : (
        <div className={carregando ? "space-y-4 opacity-60" : "space-y-4"}>{children(dados)}</div>
      )}
    </Card>
  );
}
