import { Monitor, Moon, Sun, type LucideIcon } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { useTema, type PreferenciaTema } from "@/lib/tema";
import { cn } from "@/lib/utils";

const opcoes: { valor: PreferenciaTema; rotulo: string; descricao: string; icone: LucideIcon }[] = [
  { valor: "sistema", rotulo: "Automático", descricao: "Acompanha a preferência do seu aparelho.", icone: Monitor },
  { valor: "claro", rotulo: "Claro", descricao: "Fundo claro, o padrão da Bella.", icone: Sun },
  { valor: "dark", rotulo: "Dark", descricao: "Fundo escuro, mais confortável à noite.", icone: Moon },
];

export function AjustesPage() {
  const { preferencia, escolher } = useTema();

  return (
    <div className="space-y-4">
      <CabecalhoPagina titulo="Ajustes" frase="Escolha como a Bella aparece para você." />

      <Card>
        <CardHeader>
          <CardTitle>Aparência</CardTitle>
        </CardHeader>
        <CardContent>
          <div role="radiogroup" aria-label="Tema" className="grid gap-4 sm:grid-cols-3">
            {opcoes.map((opcao) => {
              const Icone = opcao.icone;
              const selecionada = preferencia === opcao.valor;
              return (
                <button
                  key={opcao.valor}
                  type="button"
                  role="radio"
                  aria-checked={selecionada}
                  onClick={() => escolher(opcao.valor)}
                  className={cn(
                    "flex flex-col items-start gap-2 rounded-cartao border-2 p-5 text-left outline-none transition-colors focus-visible:ring-3 focus-visible:ring-ring/50",
                    selecionada ? "border-marca bg-suave text-sobre-suave" : "border-borda hover:bg-muted",
                  )}
                >
                  <Icone className="size-6" aria-hidden="true" />
                  <span className="text-lg font-extrabold">{opcao.rotulo}</span>
                  <span className={cn("text-sm", selecionada ? "text-sobre-suave" : "text-auxiliar")}>
                    {opcao.descricao}
                  </span>
                </button>
              );
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
