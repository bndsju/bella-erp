import { formatarMoeda } from "@/lib/formatos";

export interface SerieBarras {
  nome: string;
  /** Token de cor do gráfico, ex.: "var(--grafico-2)". Sem cor, usa a barra do arquivo-mestre (Sálvia). */
  cor?: string;
}

export interface PontoBarras {
  rotulo: string;
  valores: number[];
  /** Destaca a barra (ex.: o dia de hoje) na cor da marca. */
  atual?: boolean;
}

interface GraficoBarrasProps {
  series: SerieBarras[];
  pontos: PontoBarras[];
  /** Mensagem quando não há nada a mostrar. */
  vazio: { titulo: string; texto: string };
  altura?: number;
  /** Gráfico sobre cartão de destaque (verde-escuro): legenda e vazio em branco. */
  sobreDestaque?: boolean;
}

/**
 * Barras do arquivo-mestre (".barras"/".barra"): grade de colunas, cantos de 12 px, rótulo de 13 px abaixo.
 * Cada coluna pode ter mais de uma barra (uma por série); a altura é proporcional ao maior valor.
 */
export function GraficoBarras({ series, pontos, vazio, altura = 190, sobreDestaque = false }: GraficoBarrasProps) {
  const maximo = Math.max(0, ...pontos.flatMap((ponto) => ponto.valores));

  if (pontos.length === 0 || maximo === 0) {
    return (
      <div className="vazio [&_h3]:text-inherit [&_p]:text-inherit">
        <h3>{vazio.titulo}</h3>
        <p>{vazio.texto}</p>
      </div>
    );
  }

  const espaco = pontos.length > 12 ? 4 : 12;
  const passoRotulo = Math.ceil(pontos.length / 8);
  const alturaUtil = altura - 28;

  return (
    <div>
      <div
        className="barras"
        style={{ gridTemplateColumns: `repeat(${pontos.length}, minmax(0, 1fr))`, gap: espaco, height: altura }}
        role="img"
        aria-label={series.map((serie) => serie.nome).join(" e ")}
      >
        {pontos.map((ponto, indice) => (
          <div key={`${ponto.rotulo}-${indice}`} className={ponto.atual ? "barra barra--atual" : "barra"}>
            <div className="flex items-end justify-center gap-0.5" style={{ height: alturaUtil }}>
              {ponto.valores.map((valor, posicao) => (
                <i
                  key={series[posicao].nome}
                  title={`${ponto.rotulo} — ${series[posicao].nome}: ${formatarMoeda(valor)}`}
                  style={{
                    flex: 1,
                    height: Math.max(valor > 0 ? 4 : 0, (valor / maximo) * alturaUtil),
                    ...(series[posicao].cor && !ponto.atual ? { background: series[posicao].cor } : {}),
                  }}
                />
              ))}
            </div>
            <span style={{ visibility: indice % passoRotulo === 0 ? "visible" : "hidden" }}>{ponto.rotulo}</span>
          </div>
        ))}
      </div>
      {series.some((serie) => serie.cor) && (
        <div className="legenda mt-3 flex-row flex-wrap gap-x-4" style={sobreDestaque ? { color: "#FFFFFF" } : undefined}>
          {series.map((serie) => (
            <span key={serie.nome}>
              <i style={{ background: serie.cor }} />
              {serie.nome}
            </span>
          ))}
        </div>
      )}
    </div>
  );
}
