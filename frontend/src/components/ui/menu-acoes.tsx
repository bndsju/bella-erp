import { Fragment } from "react";
import { Link } from "react-router-dom";
import { DropdownMenu } from "radix-ui";
import { Ellipsis, type LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/authContexto";
import { cn } from "@/lib/utils";
import type { AcaoPermissao } from "@/types/permissao";

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

/**
 * Permissão que cada ação exige, pelo nome dela. "Abrir" e "Histórico" só consultam (não pedem nada além de ver a
 * tela); nomes que não se reconhecem também não são barrados aqui, a API continua sendo quem decide.
 */
function acaoExigida(rotulo: string): AcaoPermissao | null {
  if (/^editar/i.test(rotulo) || /^realizar/i.test(rotulo)) return "EDITAR";
  if (/^(ativar|inativar|cancelar)/i.test(rotulo)) return "CANCELAR";
  if (/^(registrar|movimentar)/i.test(rotulo)) return "CRIAR";
  return null;
}

/**
 * Menu de ações de uma linha de tabela (botão "⋯"), no mesmo painel escuro do submenu do menu principal.
 * Ações destrutivas ficam no fim, separadas. Sem nenhum item visível, o menu não é desenhado. Com {@code modulo},
 * esconde as ações que o perfil do usuário não pode executar naquele módulo.
 */
export function MenuAcoes({ itens, modulo }: { itens: ItemAcao[]; modulo?: string }) {
  const { pode } = useAuth();
  const visiveis = itens.filter((item) => {
    if (item.oculto) return false;
    const exigida = modulo ? acaoExigida(item.rotulo) : null;
    return !modulo || !exigida || pode(modulo, exigida);
  });
  if (visiveis.length === 0) return null;

  const primeiroPerigo = visiveis.findIndex((item) => item.perigo);

  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger asChild>
        <Button
          variant="secondary"
          size="icon"
          aria-label="Mais ações"
          onClick={(evento) => evento.stopPropagation()}
        >
          <Ellipsis className="size-4" />
        </Button>
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content
          align="end"
          sideOffset={6}
          className="painel"
          onClick={(evento) => evento.stopPropagation()}
        >
          {visiveis.map((item, indice) => {
            const Icone = item.icone;
            const classe = cn("painel__item", item.perigo && "painel__item--perigo");
            const conteudo = (
              <>
                <Icone className="size-4 shrink-0" aria-hidden="true" />
                {item.rotulo}
              </>
            );
            return (
              <Fragment key={item.rotulo}>
                {indice === primeiroPerigo && indice > 0 && <DropdownMenu.Separator className="painel__separador" />}
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
