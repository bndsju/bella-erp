import { Fragment } from "react";
import { Link } from "react-router-dom";
import { DropdownMenu } from "radix-ui";
import { Ellipsis, type LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export interface ItemAcao {
  rotulo: string;
  icone: LucideIcon;
  /** Ação executada ao clicar. */
  aoClicar?: () => void;
  /** Alternativa a aoClicar: navega para a rota. */
  para?: string;
  /** Ação destrutiva (inativar, cancelar...): aparece em vermelho, separada das demais. */
  perigo?: boolean;
  desabilitado?: boolean;
  /** Item que não se aplica ao registro: simplesmente não é exibido. */
  oculto?: boolean;
}

const CLASSE_ITEM =
  "flex cursor-pointer select-none items-center gap-2 rounded-full px-3.5 py-2 text-sm font-semibold outline-none transition-colors " +
  "data-[disabled]:pointer-events-none data-[disabled]:opacity-50";

/**
 * Menu de ações de uma linha de tabela (botão "⋯"). Ações destrutivas ficam no fim, separadas e em
 * vermelho. Sem nenhum item visível, o menu não é desenhado.
 */
export function MenuAcoes({ itens }: { itens: ItemAcao[] }) {
  const visiveis = itens.filter((item) => !item.oculto);
  if (visiveis.length === 0) return null;

  const primeiroPerigo = visiveis.findIndex((item) => item.perigo);

  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="size-8 text-muted-foreground hover:text-foreground"
          aria-label="Mais ações"
          onClick={(evento) => evento.stopPropagation()}
        >
          <Ellipsis className="size-4" />
        </Button>
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content
          align="end"
          sideOffset={4}
          className="z-50 min-w-48 rounded-2xl border border-border bg-popover p-1.5 text-popover-foreground"
          onClick={(evento) => evento.stopPropagation()}
        >
          {visiveis.map((item, indice) => {
            const Icone = item.icone;
            const classe = cn(
              CLASSE_ITEM,
              item.perigo
                ? "text-destructive data-[highlighted]:bg-destructive/10"
                : "data-[highlighted]:bg-accent data-[highlighted]:text-accent-foreground",
            );
            const conteudo = (
              <>
                <Icone className="size-4 shrink-0" />
                {item.rotulo}
              </>
            );
            return (
              <Fragment key={item.rotulo}>
                {indice === primeiroPerigo && indice > 0 && (
                  <DropdownMenu.Separator className="-mx-1.5 my-1.5 h-px bg-border" />
                )}
                {item.para ? (
                  <DropdownMenu.Item asChild className={classe} disabled={item.desabilitado}>
                    <Link to={item.para}>{conteudo}</Link>
                  </DropdownMenu.Item>
                ) : (
                  <DropdownMenu.Item className={classe} disabled={item.desabilitado} onSelect={item.aoClicar}>
                    {conteudo}
                  </DropdownMenu.Item>
                )}
              </Fragment>
            );
          })}
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
}
