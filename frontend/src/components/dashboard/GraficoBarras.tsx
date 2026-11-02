import { formatarMoeda } from "@/lib/formatos";

export interface SerieBarras {
  nome: string;
  /** Classe de preenchimento do Tailwind (ex.: fill-primary). */
  classe: string;
}

export interface PontoBarras {
  rotulo: string;
  valores: number[];
}

interface GraficoBarrasProps {
  series: SerieBarras[];
  pontos: PontoBarras[];
  vazio: string;
}

const LARGURA = 640;
const ALTURA = 200;
const MARGEM_BASE = 22;
const MARGEM_TOPO = 8;

/** Gráfico de barras simples em SVG: uma barra por série em cada ponto, escala pelo maior valor. */
export function GraficoBarras({ series, pontos, vazio }: GraficoBarrasProps) {
  const maximo = Math.max(0, ...pontos.flatMap((ponto) => ponto.valores));

  if (pontos.length === 0 || maximo === 0) {
    return (
      <div className="flex h-[200px] items-center justify-center rounded-lg border border-dashed border-border text-sm text-muted-foreground">
        {vazio}
      </div>
    );
  }

  const alturaUtil = ALTURA - MARGEM_BASE - MARGEM_TOPO;
  const larguraGrupo = LARGURA / pontos.length;
  const larguraBarra = Math.max(2, Math.min(28, (larguraGrupo * 0.7) / series.length));
  const passoRotulo = Math.ceil(pontos.length / 8);

  return (
    <div className="space-y-2">
      <svg viewBox={`0 0 ${LARGURA} ${ALTURA}`} className="h-auto w-full" role="img">
        <line
          x1="0"
          x2={LARGURA}
          y1={ALTURA - MARGEM_BASE}
          y2={ALTURA - MARGEM_BASE}
          className="stroke-border"
        />
        {pontos.map((ponto, indice) => {
          const centro = indice * larguraGrupo + larguraGrupo / 2;
          const inicio = centro - (larguraBarra * series.length) / 2;
          return (
            <g key={ponto.rotulo}>
              {ponto.valores.map((valor, posicao) => {
                const altura = (valor / maximo) * alturaUtil;
                return (
                  <rect
                    key={series[posicao].nome}
                    x={inicio + posicao * larguraBarra}
                    y={ALTURA - MARGEM_BASE - altura}
                    width={larguraBarra - 1}
                    height={altura}
                    rx="2"
                    className={series[posicao].classe}
                  >
                    <title>{`${ponto.rotulo} — ${series[posicao].nome}: ${formatarMoeda(valor)}`}</title>
                  </rect>
                );
              })}
              {indice % passoRotulo === 0 && (
                <text
                  x={centro}
                  y={ALTURA - 6}
                  textAnchor="middle"
                  className="fill-muted-foreground text-[10px]"
                >
                  {ponto.rotulo}
                </text>
              )}
            </g>
          );
        })}
      </svg>
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-muted-foreground">
        <div className="flex gap-3">
          {series.map((serie) => (
            <span key={serie.nome} className="flex items-center gap-1">
              <svg width="10" height="10" aria-hidden="true">
                <rect width="10" height="10" rx="2" className={serie.classe} />
              </svg>
              {serie.nome}
            </span>
          ))}
        </div>
        <span>Maior valor: {formatarMoeda(maximo)}</span>
      </div>
    </div>
  );
}
