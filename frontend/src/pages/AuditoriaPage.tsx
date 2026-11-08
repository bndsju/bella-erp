import { useState } from "react";
import { DetalheAuditoriaDialog } from "@/components/auditoria/DetalheAuditoriaDialog";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";
import { BotaoLimpar, CampoPeriodo, CampoSelecao } from "@/components/relatorios/filtros";
import { TODOS, periodoInvalido, sem, useFiltros, type Opcao } from "@/components/relatorios/filtrosBase";
import { SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { auditoriaApi } from "@/lib/auditoriaApi";
import {
  MODULOS,
  OPERACOES,
  ROTULO_MODULO,
  resumoDoRegistro,
  rotuloOperacao,
  varianteOperacao,
} from "@/lib/auditoriaRotulos";
import { PERIODOS } from "@/lib/periodos";
import { formatarDataHora } from "@/lib/relatorioRotulos";
import type { ModuloAuditoria, OperacaoAuditoria, RegistroAuditoria } from "@/types/auditoria";

const OPCOES_MODULO: Opcao[] = MODULOS.map((valor) => ({ valor, rotulo: ROTULO_MODULO[valor] }));
const OPCOES_OPERACAO: Opcao[] = OPERACOES.map((valor) => ({
  valor,
  // "Pagamento" vale para pagar e receber; o rótulo específico aparece nas linhas.
  rotulo: valor === "REGISTRO_PAGAMENTO" ? "Pagamento / recebimento" : rotuloOperacao(valor),
}));

export function AuditoriaPage() {
  const [padraoInicio, padraoFim] = PERIODOS[2].intervalo();
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    inicio: padraoInicio,
    fim: padraoFim,
    modulo: TODOS,
    operacao: TODOS,
    usuario: "",
    pagina: 0,
  });
  // O usuário só vira filtro ao confirmar (Enter ou sair do campo), para não consultar a cada letra.
  const [usuarioDigitado, setUsuarioDigitado] = useState("");
  const [aberto, setAberto] = useState<RegistroAuditoria | null>(null);

  const aplicarUsuario = () => {
    const valor = usuarioDigitado.trim();
    if (valor !== f.usuario) mudar({ usuario: valor });
  };

  return (
    <>
      <CabecalhoPagina titulo="Auditoria" frase="Quem fez o quê e quando, em todos os módulos" />

      <SecaoRelatorio
        titulo="Registros de auditoria"
        chave={JSON.stringify(f)}
        bloqueio={periodoInvalido(f.inicio, f.fim)}
        buscar={() =>
          auditoriaApi.listar({
            inicio: f.inicio || undefined,
            fim: f.fim || undefined,
            modulo: sem(f.modulo) as ModuloAuditoria | undefined,
            operacao: sem(f.operacao) as OperacaoAuditoria | undefined,
            usuario: f.usuario || undefined,
            pagina: f.pagina,
            tamanho: 20,
          })
        }
        filtros={
          <>
            <CampoPeriodo prefixo="aud" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
            <CampoSelecao
              id="aud-modulo"
              rotulo="Módulo"
              valor={f.modulo}
              aoMudar={(modulo) => mudar({ modulo })}
              opcoes={OPCOES_MODULO}
              rotuloTodos="Todos os módulos"
            />
            <CampoSelecao
              id="aud-operacao"
              rotulo="Operação"
              valor={f.operacao}
              aoMudar={(operacao) => mudar({ operacao })}
              opcoes={OPCOES_OPERACAO}
              rotuloTodos="Todas as operações"
            />
            <div className="campo">
              <label htmlFor="aud-usuario">Responsável (nome exato)</label>
              <input
                id="aud-usuario"
                type="text"
                value={usuarioDigitado}
                placeholder="Ex.: gerente"
                onChange={(e) => setUsuarioDigitado(e.target.value)}
                onBlur={aplicarUsuario}
                onKeyDown={(e) => e.key === "Enter" && aplicarUsuario()}
              />
            </div>
            <BotaoLimpar
              visivel={alterado || usuarioDigitado !== ""}
              aoClicar={() => {
                limpar();
                setUsuarioDigitado("");
              }}
            />
          </>
        }
      >
        {(dados) =>
          dados.itens.length === 0 ? (
            <SemRegistros texto="Nenhum registro com esses filtros." />
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Data e hora</TableHead>
                    <TableHead>Responsável</TableHead>
                    <TableHead>Módulo</TableHead>
                    <TableHead>Operação</TableHead>
                    <TableHead>O que aconteceu</TableHead>
                    <TableHead className="text-right">Ações</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {dados.itens.map((registro) => (
                    <TableRow key={registro.id}>
                      <TableCell>{formatarDataHora(registro.dataHora)}</TableCell>
                      <TableCell className="tabela__forte">{registro.usuario}</TableCell>
                      <TableCell>{ROTULO_MODULO[registro.modulo]}</TableCell>
                      <TableCell>
                        <Badge variant={varianteOperacao(registro.operacao)}>
                          {rotuloOperacao(registro.operacao, registro.modulo)}
                        </Badge>
                      </TableCell>
                      <TableCell>{resumoDoRegistro(registro)}</TableCell>
                      <TableCell className="text-right">
                        <Button variant="secondary" size="sm" onClick={() => setAberto(registro)}>
                          Detalhes
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              <Paginacao pagina={dados} onMudar={irParaPagina} />
            </>
          )
        }
      </SecaoRelatorio>

      <DetalheAuditoriaDialog registro={aberto} onOpenChange={(valor) => !valor && setAberto(null)} />
    </>
  );
}
