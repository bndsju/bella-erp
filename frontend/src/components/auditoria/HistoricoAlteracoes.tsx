import { useCallback, useEffect, useState } from "react";
import { DetalheAuditoriaDialog } from "@/components/auditoria/DetalheAuditoriaDialog";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { auditoriaApi } from "@/lib/auditoriaApi";
import { resumoDoRegistro, rotuloOperacao, varianteOperacao } from "@/lib/auditoriaRotulos";
import { formatarDataHora } from "@/lib/relatorioRotulos";
import type { ModuloAuditoria, RegistroAuditoria } from "@/types/auditoria";
import type { Pagina } from "@/types/relatorio";

interface HistoricoAlteracoesProps {
  modulo: ModuloAuditoria;
  entidadeId: string;
  /** Muda quando o registro muda (ex.: o status); o histórico é consultado de novo. */
  atualizarCom?: unknown;
}

/** Linha do tempo de um registro: quem fez o quê e quando, da operação mais recente para a mais antiga. */
export function HistoricoAlteracoes({ modulo, entidadeId, atualizarCom }: HistoricoAlteracoesProps) {
  const [pagina, setPagina] = useState(0);
  const [dados, setDados] = useState<Pagina<RegistroAuditoria> | null>(null);
  const [falhou, setFalhou] = useState(false);
  const [aberto, setAberto] = useState<RegistroAuditoria | null>(null);

  const carregar = useCallback(() => {
    setFalhou(false);
    auditoriaApi
      .historico(modulo, entidadeId, pagina, 10)
      .then(setDados)
      .catch(() => setFalhou(true));
  }, [modulo, entidadeId, pagina]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(carregar, [carregar, atualizarCom]);

  return (
    <Card>
      <CardTitle>Histórico de alterações</CardTitle>
      {falhou ? (
        <div className="vazio">
          <h3>Não consegui carregar o histórico</h3>
          <Button onClick={carregar}>Tentar de novo</Button>
        </div>
      ) : !dados ? (
        <Skeleton className="h-24 w-full rounded-[20px]" />
      ) : dados.itens.length === 0 ? (
        <p>Nenhuma alteração registrada para este item.</p>
      ) : (
        <>
          <div>
            {dados.itens.map((registro) => (
              <div key={registro.id} className="linha">
                <span className="flex min-w-0 flex-col gap-1">
                  <span className="flex flex-wrap items-center gap-2">
                    <Badge variant={varianteOperacao(registro.operacao)}>
                      {rotuloOperacao(registro.operacao, registro.modulo)}
                    </Badge>
                    <strong>{registro.usuario}</strong>
                    <span className="text-[13px] text-muted-foreground">{formatarDataHora(registro.dataHora)}</span>
                  </span>
                  <span className="text-[14px]">{resumoDoRegistro(registro)}</span>
                </span>
                <Button variant="secondary" size="sm" onClick={() => setAberto(registro)}>
                  Detalhes
                </Button>
              </div>
            ))}
          </div>
          <Paginacao pagina={dados} onMudar={setPagina} />
        </>
      )}
      <DetalheAuditoriaDialog registro={aberto} onOpenChange={(valor) => !valor && setAberto(null)} />
    </Card>
  );
}
