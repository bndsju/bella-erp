import { Card, CardTitle } from "@/components/ui/card";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { useTema, type PreferenciaTema } from "@/lib/tema";

const opcoes: { valor: PreferenciaTema; rotulo: string }[] = [
  { valor: "claro", rotulo: "Claro" },
  { valor: "dark", rotulo: "Dark" },
  { valor: "sistema", rotulo: "Igual ao aparelho" },
];

/** Configuração: opções agrupadas em cartões. Hoje só a aparência; a escolha fica salva neste aparelho. */
export function AjustesPage() {
  const { preferencia, escolher } = useTema();

  return (
    <>
      <CabecalhoPagina titulo="Ajustes" frase="Preferências do sistema" />

      <Card>
        <CardTitle>Aparência</CardTitle>
        <p className="mb-3">Escolha o tema do sistema.</p>
        <div className="opcoes-tema" role="group" aria-label="Tema">
          {opcoes.map((opcao) => (
            <button
              key={opcao.valor}
              type="button"
              className="pagamento"
              style={{ padding: "0 16px" }}
              aria-pressed={preferencia === opcao.valor}
              onClick={() => escolher(opcao.valor)}
            >
              {opcao.rotulo}
            </button>
          ))}
        </div>
      </Card>
    </>
  );
}
