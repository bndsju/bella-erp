import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { RelatoriosComerciais } from "@/components/relatorios/RelatoriosComerciais";
import { RelatoriosEstoque } from "@/components/relatorios/RelatoriosEstoque";
import { RelatoriosFinanceiros } from "@/components/relatorios/RelatoriosFinanceiros";
import { PERIODOS } from "@/lib/periodos";

export function RelatoriosPage() {
  const [padraoInicio, padraoFim] = PERIODOS[0].intervalo();
  const [inicio, setInicio] = useState(padraoInicio);
  const [fim, setFim] = useState(padraoFim);

  const valido = Boolean(inicio) && Boolean(fim) && fim >= inicio;

  return (
    <>
      <CabecalhoPagina titulo="Relatórios" frase="Números do negócio por período" />

      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="rel-de">Período geral: de</label>
            <input id="rel-de" type="date" value={inicio} onChange={(e) => setInicio(e.target.value)} />
          </div>
          <div className="campo">
            <label htmlFor="rel-ate">Até</label>
            <input id="rel-ate" type="date" value={fim} onChange={(e) => setFim(e.target.value)} />
          </div>
          {PERIODOS.map((periodo) => (
            <Button
              key={periodo.rotulo}
              variant="secondary"
              onClick={() => {
                const [de, ate] = periodo.intervalo();
                setInicio(de);
                setFim(ate);
              }}
            >
              {periodo.rotulo}
            </Button>
          ))}
        </div>
        <p className="mt-3 text-[13px] text-muted-foreground">
          O período geral é o ponto de partida: cada relatório tem seus próprios filtros, inclusive as datas. Mudar o
          período geral reinicia os filtros dos relatórios da aba.
        </p>
        {!valido && inicio && fim && (
          <p className="mt-3 text-sm font-semibold text-erro">A data final não pode ser anterior à inicial.</p>
        )}
      </Card>

      {valido && (
        <Tabs defaultValue="comercial">
          <TabsList>
            <TabsTrigger value="comercial">Comercial</TabsTrigger>
            <TabsTrigger value="financeiro">Financeiro</TabsTrigger>
            <TabsTrigger value="estoque">Estoque</TabsTrigger>
          </TabsList>
          <TabsContent value="comercial" className="space-y-4">
            <RelatoriosComerciais key={`${inicio}|${fim}`} inicio={inicio} fim={fim} />
          </TabsContent>
          <TabsContent value="financeiro" className="space-y-4">
            <RelatoriosFinanceiros key={`${inicio}|${fim}`} inicio={inicio} fim={fim} />
          </TabsContent>
          <TabsContent value="estoque" className="space-y-4">
            <RelatoriosEstoque key={`${inicio}|${fim}`} inicio={inicio} fim={fim} />
          </TabsContent>
        </Tabs>
      )}
    </>
  );
}
