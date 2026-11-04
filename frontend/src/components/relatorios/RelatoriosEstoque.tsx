import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Indicador, SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { categoriaApi } from "@/lib/categoriaApi";
import { ROTULO_ORIGEM, ROTULO_TIPO, formatarQuantidade, quantidadeComSinal } from "@/lib/estoqueFormatos";
import { relatorioApi } from "@/lib/relatorioApi";
import { formatarDataHora } from "@/lib/relatorioRotulos";
import type { CategoriaResponse } from "@/types/categoria";
import type { TipoMovimentacaoEstoque } from "@/types/estoque";

const TODOS = "TODOS";

interface Props {
  inicio: string;
  fim: string;
}

function SecaoPosicao() {
  const [categorias, setCategorias] = useState<CategoriaResponse[]>([]);
  const [categoriaId, setCategoriaId] = useState(TODOS);
  const [apenasAbaixo, setApenasAbaixo] = useState(false);
  const [pagina, setPagina] = useState(0);

  useEffect(() => {
    categoriaApi.listar().then(setCategorias).catch(() => setCategorias([]));
  }, []);

  return (
    <SecaoRelatorio
      titulo="Posição do estoque"
      chave={`${categoriaId}|${apenasAbaixo}|${pagina}`}
      buscar={() =>
        relatorioApi.estoquePosicao({
          categoriaId: categoriaId === TODOS ? undefined : categoriaId,
          apenasAbaixoDoMinimo: apenasAbaixo,
          pagina,
          tamanho: 10,
        })
      }
      filtros={
        <>
          <div className="campo">
            <label htmlFor="rel-estoque-categoria">Categoria</label>
            <Select
              value={categoriaId}
              onValueChange={(valor) => {
                setCategoriaId(valor);
                setPagina(0);
              }}
            >
              <SelectTrigger id="rel-estoque-categoria" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={TODOS}>Todas</SelectItem>
                {categorias.map((categoria) => (
                  <SelectItem key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <label className="flex min-h-11 items-center gap-2 text-sm font-semibold">
            <input
              type="checkbox"
              checked={apenasAbaixo}
              onChange={(e) => {
                setApenasAbaixo(e.target.checked);
                setPagina(0);
              }}
            />
            Só abaixo do mínimo
          </label>
        </>
      }
    >
      {(dados) => (
        <>
          <div className="grade grade--2">
            <Indicador rotulo="Produtos" valor={String(dados.totalProdutos)} />
            <Indicador rotulo="Abaixo do mínimo" valor={String(dados.produtosAbaixoDoMinimo)} />
          </div>
          {dados.produtos.itens.length === 0 ? (
            <SemRegistros texto="Nenhum produto com esses filtros." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Produto</TableHead>
                  <TableHead>Categoria</TableHead>
                  <TableHead className="text-right">Físico</TableHead>
                  <TableHead className="text-right">Reservado</TableHead>
                  <TableHead className="text-right">Disponível</TableHead>
                  <TableHead className="text-right">Mínimo</TableHead>
                  <TableHead>Situação</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.produtos.itens.map((produto) => (
                  <TableRow key={produto.produtoId}>
                    <TableCell className="tabela__forte">
                      {produto.codigoInterno} — {produto.nome}
                    </TableCell>
                    <TableCell>{produto.categoriaNome ?? "—"}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.estoqueFisico)}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.quantidadeReservada)}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.quantidadeDisponivel)}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.estoqueMinimo)}</TableCell>
                    <TableCell>
                      {produto.abaixoDoMinimo ? (
                        <Badge variant="destructive">Repor</Badge>
                      ) : (
                        <Badge variant="default">Em dia</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.produtos} onMudar={setPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoMovimentacoes({ inicio, fim }: Props) {
  const [tipo, setTipo] = useState(TODOS);
  const [pagina, setPagina] = useState(0);
  return (
    <SecaoRelatorio
      titulo="Movimentações de estoque"
      chave={`${inicio}|${fim}|${tipo}|${pagina}`}
      buscar={() =>
        relatorioApi.estoqueMovimentacoes({
          inicio,
          fim,
          tipo: tipo === TODOS ? undefined : tipo,
          pagina,
          tamanho: 10,
        })
      }
      filtros={
        <div className="campo">
          <label htmlFor="rel-estoque-tipo">Tipo</label>
          <Select
            value={tipo}
            onValueChange={(valor) => {
              setTipo(valor);
              setPagina(0);
            }}
          >
            <SelectTrigger id="rel-estoque-tipo" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TODOS}>Todos</SelectItem>
              {(Object.keys(ROTULO_TIPO) as TipoMovimentacaoEstoque[]).map((valor) => (
                <SelectItem key={valor} value={valor}>
                  {ROTULO_TIPO[valor]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      }
    >
      {(dados) => (
        <>
          {dados.totaisPorTipo.length > 0 && (
            <div className="grade grade--3">
              {dados.totaisPorTipo.map((total) => (
                <Indicador
                  key={total.tipo}
                  rotulo={ROTULO_TIPO[total.tipo]}
                  valor={formatarQuantidade(total.quantidadeTotal)}
                  nota={`${total.quantidadeMovimentacoes} movimentação(ões)`}
                />
              ))}
            </div>
          )}
          {dados.movimentacoes.itens.length === 0 ? (
            <SemRegistros texto="Nenhuma movimentação neste período." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Data</TableHead>
                  <TableHead>Produto</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead className="text-right">Quantidade</TableHead>
                  <TableHead>Origem</TableHead>
                  <TableHead>Responsável</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.movimentacoes.itens.map((mov) => (
                  <TableRow key={mov.id}>
                    <TableCell>{formatarDataHora(mov.dataHora)}</TableCell>
                    <TableCell className="tabela__forte">{mov.produtoNome}</TableCell>
                    <TableCell>{ROTULO_TIPO[mov.tipo]}</TableCell>
                    <TableCell className="text-right">{quantidadeComSinal(mov.tipo, mov.quantidade)}</TableCell>
                    <TableCell>{ROTULO_ORIGEM[mov.origem]}</TableCell>
                    <TableCell>{mov.usuarioResponsavel ?? "—"}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.movimentacoes} onMudar={setPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

export function RelatoriosEstoque({ inicio, fim }: Props) {
  return (
    <>
      <SecaoPosicao />
      <SecaoMovimentacoes inicio={inicio} fim={fim} />
    </>
  );
}
