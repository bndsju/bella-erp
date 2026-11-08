import { Button } from "@/components/ui/button";
import { SelecaoPesquisavel, type OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";
import { TODOS, type Opcao } from "@/components/relatorios/filtrosBase";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface CampoPeriodoProps {
  prefixo: string;
  rotuloDe?: string;
  rotuloAte?: string;
  inicio: string;
  fim: string;
  aoMudar: (parte: { inicio?: string; fim?: string }) => void;
}

export function CampoPeriodo({
  prefixo,
  rotuloDe: de = "De",
  rotuloAte: ate = "Até",
  inicio,
  fim,
  aoMudar,
}: CampoPeriodoProps) {
  return (
    <>
      <div className="campo">
        <label htmlFor={`${prefixo}-de`}>{de}</label>
        <input id={`${prefixo}-de`} type="date" value={inicio} onChange={(e) => aoMudar({ inicio: e.target.value })} />
      </div>
      <div className="campo">
        <label htmlFor={`${prefixo}-ate`}>{ate}</label>
        <input id={`${prefixo}-ate`} type="date" value={fim} onChange={(e) => aoMudar({ fim: e.target.value })} />
      </div>
    </>
  );
}

interface CampoSelecaoProps {
  id: string;
  rotulo: string;
  valor: string;
  aoMudar: (valor: string) => void;
  opcoes: Opcao[];
  /** Texto da opção "sem filtro"; sem ele, a seleção não tem essa opção (ex.: "mostrar 10 primeiros"). */
  rotuloTodos?: string;
}

export function CampoSelecao({ id, rotulo, valor, aoMudar, opcoes, rotuloTodos }: CampoSelecaoProps) {
  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      <Select value={valor} onValueChange={aoMudar}>
        <SelectTrigger id={id} className="w-full">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {rotuloTodos && <SelectItem value={TODOS}>{rotuloTodos}</SelectItem>}
          {opcoes.map((opcao) => (
            <SelectItem key={opcao.valor} value={opcao.valor}>
              {opcao.rotulo}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

interface CampoPesquisaProps {
  id: string;
  rotulo: string;
  valor: string;
  aoMudar: (valor: string) => void;
  opcoes: OpcaoSelecao[];
  rotuloTodos: string;
  placeholder: string;
}

/** Seleção com busca por texto: para listas longas (clientes, produtos). */
export function CampoPesquisa({ id, rotulo, valor, aoMudar, opcoes, rotuloTodos, placeholder }: CampoPesquisaProps) {
  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      <SelecaoPesquisavel
        id={id}
        value={valor}
        onChange={aoMudar}
        opcoes={opcoes}
        valorTodos={TODOS}
        rotuloTodos={rotuloTodos}
        placeholderBusca={placeholder}
      />
    </div>
  );
}

export function CampoMarcar({ rotulo, marcado, aoMudar }: { rotulo: string; marcado: boolean; aoMudar: (v: boolean) => void }) {
  return (
    <label className="flex min-h-11 items-center gap-2 text-sm font-semibold">
      <input type="checkbox" checked={marcado} onChange={(e) => aoMudar(e.target.checked)} />
      {rotulo}
    </label>
  );
}

const TAMANHOS = [10, 25, 50, 100];

export function CampoTamanho({ id, valor, aoMudar }: { id: string; valor: number; aoMudar: (v: number) => void }) {
  return (
    <div className="campo">
      <label htmlFor={id}>Linhas por página</label>
      <Select value={String(valor)} onValueChange={(v) => aoMudar(Number(v))}>
        <SelectTrigger id={id} className="w-full">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {TAMANHOS.map((tamanho) => (
            <SelectItem key={tamanho} value={String(tamanho)}>
              {tamanho}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

export function BotaoLimpar({ visivel, aoClicar }: { visivel: boolean; aoClicar: () => void }) {
  if (!visivel) return null;
  return (
    <Button variant="secondary" onClick={aoClicar}>
      Limpar filtros
    </Button>
  );
}
