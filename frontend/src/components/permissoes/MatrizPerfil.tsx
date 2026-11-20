import { Fragment } from "react";
import { alternarAcao, alternarTudo, temTudo } from "@/lib/permissaoRegras";
import type { AcaoPermissao, MatrizPermissoes, Permissoes } from "@/types/permissao";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

interface Props {
  matriz: MatrizPermissoes;
  permissoes: Permissoes;
  /** Administrador: mostra tudo marcado e não deixa mudar. */
  somenteLeitura: boolean;
  aoMudar: (permissoes: Permissoes) => void;
}

const CAIXA = "size-5 cursor-pointer accent-[var(--cor-marca)] disabled:cursor-not-allowed disabled:opacity-60";

/** Tabela módulo × ação, agrupada como o menu do sistema. Só mostra caixa nas ações que o módulo tem. */
export function MatrizPerfil({ matriz, permissoes, somenteLeitura, aoMudar }: Props) {
  const grupos = matriz.modulos.reduce<Record<string, typeof matriz.modulos>>((acc, modulo) => {
    (acc[modulo.grupo] ??= []).push(modulo);
    return acc;
  }, {});

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Módulo</TableHead>
          {matriz.acoes.map((acao) => (
            <TableHead key={acao.chave} className="text-center">
              {acao.rotulo}
            </TableHead>
          ))}
          <TableHead className="text-center">Tudo</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {Object.entries(grupos).map(([grupo, modulos]) => (
          <Fragment key={grupo}>
            <TableRow>
              <TableCell colSpan={matriz.acoes.length + 2} className="rotulo text-auxiliar">
                {grupo}
              </TableCell>
            </TableRow>
            {modulos.map((modulo) => (
              <TableRow key={modulo.chave}>
                <TableCell className="tabela__forte">{modulo.rotulo}</TableCell>
                {matriz.acoes.map((acao) => (
                  <TableCell key={acao.chave} className="text-center">
                    {modulo.acoes.includes(acao.chave as AcaoPermissao) ? (
                      <input
                        type="checkbox"
                        className={CAIXA}
                        aria-label={`${acao.rotulo} em ${modulo.rotulo}`}
                        checked={somenteLeitura || (permissoes[modulo.chave] ?? []).includes(acao.chave)}
                        disabled={somenteLeitura}
                        onChange={(e) => aoMudar(alternarAcao(permissoes, modulo, acao.chave, e.target.checked))}
                      />
                    ) : (
                      <span aria-label="não se aplica" className="text-auxiliar">
                        —
                      </span>
                    )}
                  </TableCell>
                ))}
                <TableCell className="text-center">
                  <input
                    type="checkbox"
                    className={CAIXA}
                    aria-label={`Tudo em ${modulo.rotulo}`}
                    checked={somenteLeitura || temTudo(permissoes, modulo)}
                    disabled={somenteLeitura}
                    onChange={(e) => aoMudar(alternarTudo(permissoes, modulo, e.target.checked))}
                  />
                </TableCell>
              </TableRow>
            ))}
          </Fragment>
        ))}
      </TableBody>
    </Table>
  );
}
