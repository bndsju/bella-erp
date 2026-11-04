import { useId, useMemo, useRef, useState } from "react";
import { Popover } from "radix-ui";
import { Check, ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

export interface OpcaoSelecao {
  valor: string;
  /** Texto mostrado na lista e no campo. */
  rotulo: string;
  /** Outros textos que também encontram a opção (ex.: nome fantasia). */
  termos?: string;
}

interface SelecaoPesquisavelProps {
  id?: string;
  value: string;
  onChange: (valor: string) => void;
  opcoes: OpcaoSelecao[];
  /** Valor e texto da opção "todos" (sempre a primeira, enquanto não há busca). */
  valorTodos: string;
  rotuloTodos: string;
  placeholderBusca?: string;
  textoVazio?: string;
  className?: string;
}

/** Sem acentos e em minúsculas, para "joao" encontrar "João". */
function normalizar(texto: string): string {
  return texto.normalize("NFD").replace(/\p{M}/gu, "").toLowerCase().trim();
}

/**
 * Seleção com busca: abre uma lista com campo de pesquisa, filtra enquanto se digita e mantém a opção
 * de escolher "todos". Teclado: setas para navegar, Enter para escolher, Esc para fechar.
 */
export function SelecaoPesquisavel({
  id,
  value,
  onChange,
  opcoes,
  valorTodos,
  rotuloTodos,
  placeholderBusca = "Buscar",
  textoVazio = "Nada encontrado",
  className,
}: SelecaoPesquisavelProps) {
  const idLista = useId();
  const [aberto, setAberto] = useState(false);
  const [busca, setBusca] = useState("");
  const [ativo, setAtivo] = useState(0);
  const lista = useRef<HTMLDivElement>(null);

  const todas = useMemo<OpcaoSelecao[]>(() => [{ valor: valorTodos, rotulo: rotuloTodos }, ...opcoes], [valorTodos, rotuloTodos, opcoes]);

  const visiveis = useMemo(() => {
    const termo = normalizar(busca);
    if (termo === "") return todas;
    return opcoes.filter((opcao) => normalizar(`${opcao.rotulo} ${opcao.termos ?? ""}`).includes(termo));
  }, [busca, opcoes, todas]);

  const selecionada = todas.find((opcao) => opcao.valor === value);

  function abrirOuFechar(novo: boolean) {
    setAberto(novo);
    if (novo) {
      setBusca("");
      setAtivo(Math.max(0, todas.findIndex((opcao) => opcao.valor === value)));
    }
  }

  function escolher(opcao: OpcaoSelecao) {
    onChange(opcao.valor);
    setAberto(false);
  }

  function aoDigitar(tecla: React.KeyboardEvent<HTMLInputElement>) {
    if (tecla.key === "ArrowDown" || tecla.key === "ArrowUp") {
      tecla.preventDefault();
      const proximo = tecla.key === "ArrowDown" ? ativo + 1 : ativo - 1;
      const limitado = Math.min(Math.max(proximo, 0), Math.max(visiveis.length - 1, 0));
      setAtivo(limitado);
      lista.current?.querySelector<HTMLElement>(`[data-indice="${limitado}"]`)?.scrollIntoView({ block: "nearest" });
    } else if (tecla.key === "Enter") {
      tecla.preventDefault();
      const opcao = visiveis[ativo];
      if (opcao) escolher(opcao);
    }
  }

  return (
    <Popover.Root open={aberto} onOpenChange={abrirOuFechar}>
      <Popover.Trigger
        id={id}
        type="button"
        role="combobox"
        aria-expanded={aberto}
        aria-controls={idLista}
        className={cn("entrada flex items-center justify-between gap-2 text-left", className)}
      >
        <span className="truncate">{selecionada?.rotulo ?? rotuloTodos}</span>
        <ChevronDown className="size-4 shrink-0 opacity-60" aria-hidden="true" />
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content
          align="start"
          sideOffset={6}
          className="z-50 w-(--radix-popover-trigger-width) min-w-64 rounded-[12px] border border-borda-campo bg-cartao p-2 text-texto"
        >
          <input
            autoFocus
            className="entrada mb-2"
            placeholder={placeholderBusca}
            aria-label={placeholderBusca}
            value={busca}
            onChange={(e) => {
              setBusca(e.target.value);
              setAtivo(0);
            }}
            onKeyDown={aoDigitar}
          />
          <div ref={lista} id={idLista} role="listbox" className="max-h-64 overflow-y-auto">
            {visiveis.length === 0 ? (
              <p className="px-3 py-3 text-auxiliar">{textoVazio}</p>
            ) : (
              visiveis.map((opcao, indice) => (
                <div
                  key={opcao.valor}
                  role="option"
                  aria-selected={opcao.valor === value}
                  data-indice={indice}
                  onClick={() => escolher(opcao)}
                  onMouseMove={() => setAtivo(indice)}
                  className={cn(
                    "flex min-h-11 cursor-pointer items-center justify-between gap-2 rounded-lg px-3 font-medium",
                    indice === ativo && "bg-selecionado",
                    opcao.valor === value && "font-bold",
                  )}
                >
                  <span className="truncate">{opcao.rotulo}</span>
                  {opcao.valor === value && <Check className="size-4 shrink-0" aria-hidden="true" />}
                </div>
              ))
            )}
          </div>
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  );
}
